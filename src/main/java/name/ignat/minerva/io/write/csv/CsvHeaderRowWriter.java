package name.ignat.minerva.io.write.csv;

import com.opencsv.CSVWriter;

import name.ignat.minerva.io.write.HeaderRowWriter;

/**
 * @author Dan Ignat
 */
class CsvHeaderRowWriter extends HeaderRowWriter<CSVWriter>
{
    @Override
    public void accept(String[] headers, CSVWriter csvWriter)
    {
        csvWriter.writeNext(headers);
    }
}
