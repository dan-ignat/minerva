package name.ignat.minerva.io.read.csv;

import static com.google.common.collect.Streams.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.opencsv.CSVReader;

import name.ignat.commons.exception.UnexpectedException;
import name.ignat.minerva.io.read.ReadMapper;
import name.ignat.minerva.io.read.SpreadsheetReader;

public class CsvReader extends SpreadsheetReader<List<String>, String>
{
    private final CSVReader csvReader;

    public CsvReader(File file)
    {
        super(file);

        try
        {
            csvReader = new CSVReader(new FileReader(file));
        }
        catch (FileNotFoundException e)
        {
            throw new UnexpectedException(e);
        }
    }

    public CsvReader(Reader reader)
    {
        super(null);

        csvReader = new CSVReader(reader);
    }

    @Override
    protected Stream<List<String>> getRowStream()
    {
        return stream(csvReader).map(Arrays::asList);
    }

    @Override
    protected CsvHeaderRowReader getHeaderRowReader(String[] columnHeaders)
    {
        return new CsvHeaderRowReader(columnHeaders);
    }

    @Override
    protected <O> CsvContentRowReader<O> getContentRowReader(Integer[] columnIndexes, ReadMapper<O> objectMapper)
    {
        return new CsvContentRowReader<O>(columnIndexes, objectMapper);
    }

    @Override
    public void close() throws IOException
    {
        csvReader.close();
    }
}
