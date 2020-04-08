package name.ignat.minerva;

import static com.google.common.flogger.FluentLogger.forEnclosingClass;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.google.common.flogger.FluentLogger;

import name.ignat.minerva.OutputFileConfig.AddressLogSheetConfig;
import name.ignat.minerva.OutputFileConfig.MessageFlagSheetConfig;
import name.ignat.minerva.io.write.excel.ExcelWriter;
import name.ignat.minerva.model.Address;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.AuditLog;
import name.ignat.minerva.model.AuditLog.AddressEntry;
import name.ignat.minerva.model.AuditLog.MessageFlag;
import name.ignat.minerva.util.Array;

@Component
public class MinervaWriter
{
    private static final FluentLogger logger = forEnclosingClass();

    private final MinervaRunConfig config;
    private final File outputFile;
    private final OutputStream outputStream;

    @Autowired
    public MinervaWriter(MinervaRunConfig config,
        /*
         * ObjectProvider here is required so that Test*MinervaApp tests don't fail, as they use a
         * ByteArrayOutputStream, and have no outputFile
         */
        @Lazy ObjectProvider<File> outputFile,
        /*
         * @Lazy here avoids creating/opening the file until it's needed for writing at the very end, which prevents
         * leaving an empty file if e.g. there's a ValidationException during processing
         */
        @Lazy OutputStream outputStream)
    {
        this.config = config;

        // FIXME: Try this with .getIfAvailable(), and then better yet with .getObject(), and see how they behave during
        // unit tests and when I configure no beans and 2 beans matching this, as it's not clear which method I should
        // call here
        this.outputFile = outputFile.getIfUnique();

        this.outputStream = outputStream;
    }

    public void writeAddressBook(AddressBook addressBook) throws IOException
    {
        try (ExcelWriter excelWriter = new ExcelWriter(outputStream))
        {
            writeAddressSheet(addressBook, excelWriter);

            writeMessageFlagSheet(addressBook.getAuditLog(), excelWriter);

            writeAddressLogSheet(addressBook.getAuditLog(), excelWriter);
        }

        if (outputFile != null)
        {
            outputFile.setReadOnly();

            logger.atInfo().log("Output file written to %s", outputFile.getCanonicalPath());
        }
    }

    private void writeAddressSheet(AddressBook addressBook, ExcelWriter excelWriter) throws IOException
    {
        SingleColumnSheetConfig sheetConfig = config.getOutputFile().getAddressSheet();

        excelWriter.setSheetName(sheetConfig.getName());

        excelWriter.write(Array.of(sheetConfig.getColumnHeader()), addressBook.getAddresses(), Address.class);
    }

    private void writeMessageFlagSheet(AuditLog auditLog, ExcelWriter excelWriter) throws IOException
    {
        MessageFlagSheetConfig sheetConfig = config.getOutputFile().getMessageFlagSheet();

        excelWriter.setSheetName(sheetConfig.getName());

        MessageFlagSheetConfig.ColumnHeadersConfig columnHeadersConfig = sheetConfig.getColumnHeaders();

        String[] columnHeaders = Array.of(
            columnHeadersConfig.getMessageIndex(),
            columnHeadersConfig.getExtractedAddresses(),
            columnHeadersConfig.getMatchedRule(),
            columnHeadersConfig.getReason());

        excelWriter.write(columnHeaders, auditLog.getMessageFlags(), MessageFlag.class);
    }

    private void writeAddressLogSheet(AuditLog auditLog, ExcelWriter excelWriter) throws IOException
    {
        AddressLogSheetConfig sheetConfig = config.getOutputFile().getAddressLogSheet();

        excelWriter.setSheetName(sheetConfig.getName());

        AddressLogSheetConfig.ColumnHeadersConfig columnHeadersConfig = sheetConfig.getColumnHeaders();

        String[] columnHeaders = Array.of(
            columnHeadersConfig.getMessageIndex(),
            columnHeadersConfig.getAction(),
            columnHeadersConfig.getAddress(),
            columnHeadersConfig.getExtractedAddresses(),
            columnHeadersConfig.getMatchedRule());

        excelWriter.write(columnHeaders, auditLog.getAddressEntries(), AddressEntry.class);
    }
}
