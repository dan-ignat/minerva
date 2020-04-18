package name.ignat.minerva;

import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.stream.Collectors.joining;
import static name.ignat.commons.utils.IoUtils.getClassPathResource;
import static name.ignat.commons.utils.ObjectUtils.equalsAny;
import static name.ignat.minerva.MinervaProfiles.PROD;
import static name.ignat.minerva.util.Files.openNewOutputFile;
import static name.ignat.minerva.util.JacksonUtils.parseYaml;
import static name.ignat.minerva.util.Strings.replaceVariables;
import static org.apache.commons.lang3.StringUtils.substringsBetween;
import static org.springframework.boot.Banner.Mode.OFF;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import com.google.common.collect.ImmutableMap;

import name.ignat.commons.exception.UnexpectedException;

/**
 * Minerva lets a user extract new email addresses from email messages, subject to user-specified filters, and using
 * various extraction rules (depending on whether the messages are auto-replies or normal replies).
 * 
 * @see README.md
 * 
 * @author Dan Ignat
 */
@SpringBootApplication
public class MinervaApp
{
    /** Relative to the class path */
    public static final String CONFIG_SCHEMA_FILE_PATH = "run.schema.json";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = ofPattern("YYYY-MM-dd HH.mm.ss");

    public static void main(String[] args)
    {
        System.setProperty("spring.profiles.active", PROD);
        System.setProperty("spring.main.banner-mode", OFF.name());

        checkUsage(args);

        SpringApplication.run(MinervaApp.class, args);
    }

    /*
     * I tried to use Picocli to handle the CLI, but it doesn't yet work with Spring Boot if your @Configuration (or
     * @SpringBootApplication) class needs to use a CLI arg in a @Bean method, like we do below in config().
     * 
     * I found only one question about this on StackOverflow:
     *      https://stackoverflow.com/questions/58946730/howto-inject-picocli-parsed-parameters-into-spring-bean-definitions
     * and althought there are no answers, the author of Picocli commented and implied there's currently no easy answer.
     * 
     * Basically it's a chicken/egg problem.  If your main method calls Picocli first to process args, nothing except
     * Strings can be passed to Spring Boot anyway.  And if your main method calls Spring Boot first to process args,
     * it runs @Configuration classes first, before calling the Picocli code.  Only workaround currently (mentioned in
     * StackOverflow question) would be to use @Lazy on @Bean methods, but that seems like an ugly hack.  Instead,
     * Picocli needs to figure out a better integration with Spring Boot (as of March 2020).
     */
    private static void checkUsage(String[] args)
    {
        if (args.length != 1 || equalsAny(args[0], "-h", "--help", "-?", "/h", "/help", "/?"))
        {
            @SuppressWarnings("preview")
            String usage = """
                Usage: java -jar minerva.jar <CONFIG_FILE>
                Generates an updated e-mail address list (XLSX format) based on an initial list, filters, and new e-mail messages.
                      <CONFIG_FILE> The YAML config file to use for this run (validated against src/main/resources/run.schema.json)
                """;

            System.err.println(usage);

            System.exit(1);
        }
        else
        {
            File configFile = new File(args[0]);

            if (!configFile.canRead())
            {
                System.err.println("Config file not found or not readable: " + configFile.getPath());

                System.exit(1);
            }
        }
    }

    @Bean
    protected MinervaRunConfig config(ApplicationArguments args) throws IOException
    {
        String configFilePath = args.getSourceArgs()[0];

        try (
            InputStream configFileIn = new FileInputStream(configFilePath);
            InputStream configSchemaFileIn = getClassPathResource(CONFIG_SCHEMA_FILE_PATH))
        {
            return parseYaml(configFileIn, MinervaRunConfig.class, configSchemaFileIn);
        }
    }

    /*
     * Need to expose the outputFile separately from the outputStream, to be able to log the variable-resolved
     * outputFile name at the end of processing.
     */
    @Bean @Lazy @Profile(PROD)
    protected File outputFile(MinervaRunConfig config) throws IOException
    {
        String messageFileTypes = config.getMessageFiles() == null ? "[ ]" : config.getMessageFiles().stream()
            .map(MainMessageFileConfig::getType).map(Object::toString).collect(joining(", ", "[ ", " ]"));

        Map<String, String> variableMap = ImmutableMap.<String, String>of(
            "dateTime",         now().format(DATE_TIME_FORMATTER),
            "messageFileTypes", messageFileTypes
        );

        String filePath = config.getOutputFile().getPath();

        String[] variableNames = substringsBetween(filePath, "{", "}");

        for (String variableName : variableNames)
        {
            if (!variableMap.containsKey(variableName))
            {
                throw new UnexpectedException(
                    format("Unexpected variable {%s} is not one of %s", variableName, variableMap.keySet()));
            }
        }

        filePath = replaceVariables(filePath, variableMap);

        return new File(filePath);
    }

    /*
     * @Lazy here avoids creating/opening the file until it's needed for writing at the very end, which prevents leaving
     * an empty file if e.g. there's a ValidationException during processing.
     */
    @Bean @Lazy @Profile(PROD)
    protected OutputStream outputStream(File outputFile) throws IOException
    {
        return openNewOutputFile(outputFile);
    }
}
