package name.ignat.minerva.io.read.excel;

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

import name.ignat.minerva.model.Address;
import name.ignat.minerva.model.Domain;
import name.ignat.minerva.model.Message;

public class TestExcelReader
{
    private static Stream<Arguments> readCases()
    {
        return Stream.of(
            Arguments.of("TestExcelReader/Addresses.xlsx", "Addresses",
                List.of("Address"), Address.class, List.of(new Address("a@b.com"), new Address("b@b.com"))
            ),
            Arguments.of("TestExcelReader/Domains.xlsx", "Domains",
                List.of("Domain"), Domain.class, List.of(new Domain("a.com"), new Domain("b.com"))
            ),
            Arguments.of("TestExcelReader/Messages.xlsx", "Messages",
                List.of("From", "Subject", "Body"), Message.class, List.of(
                    new Message(2, "a@b.com", "Hello", "How's it going?"),
                    new Message(3, "b@b.com", "Hi", "Long time no see!")
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("readCases")
    public <O> void read(String classPathResourcePath, String sheetName, List<String> columnHeaders, Class<O> objectClass,
        List<O> expectedObjects) throws IOException
    {
        File file = getClassPathResourceFile(classPathResourcePath);

        List<O> objects;
        try (ExcelReader excelReader = new ExcelReader(file))
        {
            excelReader.setSheetName(sheetName);

            objects = excelReader.read(columnHeaders.toArray(new String[0]), objectClass);
        }

        assertThat(objects, is(expectedObjects));
    }
}
