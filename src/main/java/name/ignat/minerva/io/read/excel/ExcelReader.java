package name.ignat.minerva.io.read.excel;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Streams.stream;
import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import lombok.Setter;
import name.ignat.commons.exception.UnexpectedException;
import name.ignat.minerva.MinervaException;
import name.ignat.minerva.io.read.ReadMapper;
import name.ignat.minerva.io.read.SpreadsheetReader;

/**
 * @author Dan Ignat
 */
public class ExcelReader extends SpreadsheetReader<Row, Cell>
{
    private final Workbook workbook;

    @Setter
    private String sheetName;

    public ExcelReader(File file)
    {
        super(file);

        try
        {
            workbook = WorkbookFactory.create(file, null, true);
        }
        catch (IOException e)
        {
            throw new UnexpectedException(e);
        }
    }

    public ExcelReader(InputStream inputStream)
    {
        super(null);

        try
        {
            workbook = WorkbookFactory.create(inputStream);
        }
        catch (IOException e)
        {
            throw new UnexpectedException(e);
        }
    }

    @Override
    protected Stream<Row> getRowStream()
    {
        checkNotNull(sheetName);

        Sheet sheet = workbook.getSheet(sheetName);

        if (sheet == null)
        {
            throw new MinervaException(format("No sheet found with name '%s'", sheetName));
        }

        return stream(sheet);
    }

    @Override
    protected ExcelHeaderRowReader getHeaderRowReader(String[] columnHeaders)
    {
        return new ExcelHeaderRowReader(columnHeaders);
    }

    @Override
    protected <O> ExcelContentRowReader<O> getContentRowReader(Integer[] columnIndexes, ReadMapper<O> objectMapper)
    {
        return new ExcelContentRowReader<O>(columnIndexes, objectMapper);
    }

    @Override
    public void close() throws IOException
    {
        workbook.close();
    }
}
