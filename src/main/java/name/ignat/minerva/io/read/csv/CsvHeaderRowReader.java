package name.ignat.minerva.io.read.csv;

import java.util.List;

import name.ignat.minerva.io.read.HeaderRowReader;

class CsvHeaderRowReader extends HeaderRowReader<List<String>, String>
{
    public CsvHeaderRowReader(String[] desiredColumnHeaders)
    {
        super(desiredColumnHeaders);
    }

    @Override
    protected String getCellValue(String headerCell)
    {
        return headerCell;
    }
}
