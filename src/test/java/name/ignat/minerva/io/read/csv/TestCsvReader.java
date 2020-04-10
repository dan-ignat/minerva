package name.ignat.minerva.io.read.csv;

import static name.ignat.commons.utils.IoUtils.getClassPathResourceFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import name.ignat.minerva.model.Message;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.address.Domain;

public class TestCsvReader
{
    private static Stream<Arguments> readCases()
    {
        return Stream.of(
            Arguments.of("TestCsvReader/Addresses.csv", List.of("Address"), Address.class,
                List.of(new Address("a@b.com"), new Address("b@b.com"))
            ),
            Arguments.of("TestCsvReader/Domains.csv", List.of("Domain"), Domain.class,
                List.of(new Domain("a.com"), new Domain("b.com"))
            ),
            Arguments.of("TestCsvReader/Messages.csv", List.of("From", "Subject", "Body"), Message.class,
                List.of(
                    new Message(2, "a@b.com", "Hello", "How's it going?"),
                    new Message(3, "b@b.com", "Hi", "Long time no see!")
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("readCases")
    public <O> void read(String classPathResourcePath, List<String> columnHeaders, Class<O> objectClass,
        List<O> expectedObjects) throws IOException
    {
        File file = getClassPathResourceFile(classPathResourcePath);

        List<O> objects;
        try (CsvReader csvReader = new CsvReader(file))
        {
            objects = csvReader.read(columnHeaders.toArray(new String[0]), objectClass);
        }

        assertThat(objects, is(expectedObjects));
    }
}
