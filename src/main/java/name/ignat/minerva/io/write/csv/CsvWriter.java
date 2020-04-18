package name.ignat.minerva.io.write.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.opencsv.CSVWriter;

import name.ignat.commons.exception.UnexpectedException;
import name.ignat.minerva.io.write.SpreadsheetWriter;
import name.ignat.minerva.io.write.WriteMapper;

/**
 * @author Dan Ignat
 */
public class CsvWriter extends SpreadsheetWriter<CSVWriter>
{
    private final CSVWriter csvWriter;

    public CsvWriter(File file)
    {
        super(file);

        try
        {
            csvWriter = new CSVWriter(new FileWriter(file));
        }
        catch (IOException e)
        {
            throw new UnexpectedException(e);
        }
    }

    public CsvWriter(Writer writer)
    {
        super(null);

        csvWriter = new CSVWriter(writer);
    }

    @Override
    protected CSVWriter getSheet()
    {
        return csvWriter;
    }

    @Override
    protected CsvHeaderRowWriter getHeaderRowWriter()
    {
        return new CsvHeaderRowWriter();
    }

    @Override
    protected <O> CsvContentRowWriter<O> getContentRowWriter(WriteMapper<O> objectMapper, int numColumns)
    {
        return new CsvContentRowWriter<O>(objectMapper, numColumns);
    }

    @Override
    protected void finishSheet(CSVWriter sheet, int numColumns)
    {
    }

    @Override
    public void close() throws IOException
    {
        csvWriter.close();
    }
}
