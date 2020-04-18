package name.ignat.minerva.io.read.csv;

import java.util.List;

import javax.annotation.Nonnull;

import name.ignat.minerva.io.read.HeaderRowReader;

/**
 * @author Dan Ignat
 */
class CsvHeaderRowReader extends HeaderRowReader<List<String>, String>
{
    public CsvHeaderRowReader(String[] desiredColumnHeaders)
    {
        super(desiredColumnHeaders);
    }

    @Override
    protected String getCellValue(@Nonnull String headerCell)
    {
        return headerCell;
    }
}
