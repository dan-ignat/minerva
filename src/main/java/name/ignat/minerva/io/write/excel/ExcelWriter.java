package name.ignat.minerva.io.write.excel;

import static com.google.common.base.Preconditions.checkNotNull;
import static name.ignat.minerva.util.PoiUtils.autoSizeColumns;
import static name.ignat.minerva.util.PoiUtils.emboldenHeaderRow;
import static name.ignat.minerva.util.PoiUtils.createHeaderStyle;
import static name.ignat.minerva.util.PoiUtils.freezeHeaderRow;
import static name.ignat.minerva.util.PoiUtils.setAutoFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import lombok.Setter;
import name.ignat.commons.exception.UnexpectedException;
import name.ignat.minerva.io.write.SpreadsheetWriter;
import name.ignat.minerva.io.write.WriteMapper;

public class ExcelWriter extends SpreadsheetWriter<Sheet>
{
    private final OutputStream outputStream;

    private final Workbook workbook;

    // Create a single shared header style per workbook, for efficiency
    private final CellStyle headerStyle;

    @Setter
    private String sheetName;

    public ExcelWriter(File file)
    {
        this(file, null);
    }

    public ExcelWriter(OutputStream outputStream)
    {
        this(null, outputStream);
    }

    private ExcelWriter(File file, OutputStream outputStream)
    {
        super(file);

        this.outputStream = outputStream;

        try
        {
            workbook = WorkbookFactory.create(true);
        }
        catch (IOException e)
        {
            throw new UnexpectedException(e);
        }

        headerStyle = createHeaderStyle(workbook);
    }

    @Override
    protected Sheet getSheet()
    {
        checkNotNull(sheetName);

        return workbook.createSheet(sheetName);
    }

    @Override
    protected ExcelHeaderRowWriter getHeaderRowWriter()
    {
        return new ExcelHeaderRowWriter();
    }

    @Override
    protected <O> ExcelContentRowWriter<O> getContentRowWriter(WriteMapper<O> objectMapper, int expectedLength)
    {
        return new ExcelContentRowWriter<O>(objectMapper, expectedLength);
    }

    @Override
    protected void finishSheet(Sheet sheet, int numColumns)
    {
        emboldenHeaderRow(sheet, headerStyle);

        setAutoFilter(sheet);

        autoSizeColumns(sheet);

        freezeHeaderRow(sheet);
    }

    @Override
    public void close() throws IOException
    {
        try (OutputStream outputStream =
            this.outputStream == null ? new FileOutputStream(getFile()) : this.outputStream)
        {
            workbook.write(outputStream);
            workbook.close();
        }
    }
}
