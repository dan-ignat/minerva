package name.ignat.minerva.io.read.csv;

import java.util.List;

import javax.annotation.Nonnull;

import name.ignat.minerva.io.read.ContentRowReader;
import name.ignat.minerva.io.read.ReadMapper;

/**
 * @author Dan Ignat
 */
class CsvContentRowReader<O> extends ContentRowReader<List<String>, String, O>
{
    public CsvContentRowReader(Integer[] columnIndexes, ReadMapper<O> objectMapper)
    {
        super(columnIndexes, objectMapper);
    }

    @Override
    protected String getCell(@Nonnull List<String> row, int index)
    {
        if (index < row.size())
        {
            return row.get(index);
        }
        else
        {
            return null;
        }
    }

    @Override
    protected String getCellValue(@Nonnull String cell)
    {
        return cell;
    }
}
