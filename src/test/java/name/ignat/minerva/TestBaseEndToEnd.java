package name.ignat.minerva;

import static name.ignat.commons.io.Resources.getResourceFile;
import static name.ignat.minerva.MinervaProfiles.TEST;
import static name.ignat.minerva.util.Lists.arraysToLists;
import static name.ignat.minerva.util.PoiUtils.sheetToStrings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
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

/**
 * @author Dan Ignat
 */
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

    protected void run(String expectedOutputFileResourcePath) throws IOException
    {
        SpringApplication.run(MinervaApp.class, args.getSourceArgs());

        File expectedOutputFile = getResourceFile(expectedOutputFileResourcePath);

        try (Workbook expectedWorkbook = WorkbookFactory.create(expectedOutputFile, null, true);
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            Workbook workbook = WorkbookFactory.create(inputStream))
        {
            assertThat(expectedWorkbook.getNumberOfSheets(), is(3));

            {
                Sheet expectedAddressSheet = expectedWorkbook.getSheet(config.getOutputFile().getAddressSheet().getName());
                List<List<String>> expectedAddressRows = arraysToLists(sheetToStrings(expectedAddressSheet));

                Sheet addressSheet = workbook.getSheet(config.getOutputFile().getAddressSheet().getName());
                List<List<String>> addressRows = arraysToLists(sheetToStrings(addressSheet));

                assertThat(addressRows, is(expectedAddressRows));
            }

            {
                Sheet expectedAddressLogSheet = expectedWorkbook.getSheet(config.getOutputFile().getAddressLogSheet().getName());
                List<List<String>> expectedAddressLogRows = arraysToLists(sheetToStrings(expectedAddressLogSheet));

                Sheet addressLogSheet = workbook.getSheet(config.getOutputFile().getAddressLogSheet().getName());
                List<List<String>> addressLogRows = arraysToLists(sheetToStrings(addressLogSheet));

                assertThat(addressLogRows, is(expectedAddressLogRows));
            }

            {
                Sheet expectedMessageFlagSheet = expectedWorkbook.getSheet(config.getOutputFile().getMessageFlagSheet().getName());
                List<List<String>> expectedMessageFlagRows = arraysToLists(sheetToStrings(expectedMessageFlagSheet));

                Sheet messageFlagSheet = workbook.getSheet(config.getOutputFile().getMessageFlagSheet().getName());
                List<List<String>> messageFlagRows = arraysToLists(sheetToStrings(messageFlagSheet));

                assertThat(messageFlagRows, is(expectedMessageFlagRows));
            }
        }
    }
}
