package name.ignat.minerva.io.write.csv;

import com.opencsv.CSVWriter;

import name.ignat.minerva.io.write.WriteMapper;
import name.ignat.minerva.io.write.ContentRowWriter;

/**
 * @author Dan Ignat
 */
class CsvContentRowWriter<O> extends ContentRowWriter<O, CSVWriter>
{
    public CsvContentRowWriter(WriteMapper<O> objectMapper, int numColumns)
    {
        super(objectMapper, numColumns);
    }

    @Override
    protected void writeRow(String[] values, CSVWriter csvWriter)
    {
        csvWriter.writeNext(values);
    }
}
