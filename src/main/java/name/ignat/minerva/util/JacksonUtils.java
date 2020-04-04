package name.ignat.minerva.util;

import static com.networknt.schema.SpecVersion.VersionFlag.V201909;
import static name.ignat.minerva.util.Objects.toLines;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

public final class JacksonUtils
{
    public static <T> T parseJson(InputStream jsonIn, Class<T> clazz)
        throws IOException, ValidationException
    {
        return parse(jsonIn, clazz, new JsonFactory(), null);
    }

    public static <T> T parseJson(InputStream jsonIn, Class<T> clazz, @Nullable InputStream jsonSchemaIn)
        throws IOException, ValidationException
    {
        return parse(jsonIn, clazz, new JsonFactory(), jsonSchemaIn);
    }

    public static <T> T parseYaml(InputStream yamlIn, Class<T> clazz)
        throws IOException, ValidationException
    {
        return parse(yamlIn, clazz, new YAMLFactory(), null);
    }

    public static <T> T parseYaml(InputStream yamlIn, Class<T> clazz, @Nullable InputStream jsonSchemaIn)
        throws IOException, ValidationException
    {
        return parse(yamlIn, clazz, new YAMLFactory(), jsonSchemaIn);
    }

    private static <T> T parse(InputStream fileIn, Class<T> clazz, JsonFactory jsonFactory, @Nullable InputStream jsonSchemaIn)
        throws IOException, ValidationException
    {
        ObjectMapper mapper = new ObjectMapper(jsonFactory);

        byte[] fileBytes = IOUtils.toByteArray(fileIn);

        if (jsonSchemaIn != null)
        {
            validate(fileBytes, jsonSchemaIn, mapper);
        }

        return mapper.readValue(fileBytes, clazz);
    }

    private static void validate(byte[] fileBytes, InputStream jsonSchemaFileIn, ObjectMapper mapper)
        throws IOException, ValidationException
    {
        JsonSchemaFactory factory =
            JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(V201909)).objectMapper(mapper).build();

        JsonSchema schema = factory.getSchema(jsonSchemaFileIn);

        JsonNode jsonNode = mapper.readTree(fileBytes);

        Set<ValidationMessage> validationMessages = schema.validate(jsonNode);

        if (!validationMessages.isEmpty())
        {
            throw new ValidationException("Failed JSON Schema validation: " + toLines(validationMessages));
        }
    }

    private JacksonUtils() { }

    @SuppressWarnings("serial")
    public static class ValidationException extends RuntimeException
    {
        public ValidationException(String message)
        {
            super(message);
        }
    }
}
