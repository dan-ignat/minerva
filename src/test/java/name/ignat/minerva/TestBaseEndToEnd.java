package name.ignat.minerva;

import static name.ignat.minerva.MinervaProfiles.TEST;
import static name.ignat.minerva.util.Lists.arraysToLists;
import static name.ignat.minerva.util.PoiUtils.sheetToStrings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(TEST)
public abstract class TestBaseEndToEnd
{
    @TestConfiguration
    protected static class TestConfig
    {
        @Bean
        protected ByteArrayOutputStream outputStream(MinervaRunConfig config)
        {
            return new ByteArrayOutputStream();
        }
    }

    @Autowired
    protected ApplicationArguments args;

    @Autowired
    protected MinervaRunConfig config;

    @Autowired
    protected ByteArrayOutputStream outputStream;

    protected void run(List<List<String>> expectedAddressRows, List<List<String>> expectedAddressLogRows,
        List<List<String>> expectedMessageFlagRows) throws IOException
    {
        SpringApplication.run(MinervaApp.class, args.getSourceArgs());

        Workbook workbook;
        try (InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray()))
        {
            workbook = WorkbookFactory.create(inputStream);
        }

        {
            Sheet addressSheet = workbook.getSheet(config.getOutputFile().getAddressSheet().getName());

            List<String[]> addressRows = sheetToStrings(addressSheet);

            assertThat(arraysToLists(addressRows), is(expectedAddressRows));
        }

        {
            Sheet addressLogSheet = workbook.getSheet(config.getOutputFile().getAddressLogSheet().getName());

            List<String[]> addressLogRows = sheetToStrings(addressLogSheet);

            assertThat(arraysToLists(addressLogRows), is(expectedAddressLogRows));
        }

        {
            Sheet messageFlagSheet = workbook.getSheet(config.getOutputFile().getMessageFlagSheet().getName());

            List<String[]> messageFlagRows = sheetToStrings(messageFlagSheet);

            assertThat(arraysToLists(messageFlagRows), is(expectedMessageFlagRows));
        }
    }
}
