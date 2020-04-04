package name.ignat.minerva.io.read;

import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * @param <R> the row type (input)
 * @param <C> the cell type (input)
 * @param <O> the object type (output)
 * 
 * @author Dan Ignat
 */
public abstract class ContentRowReader<R extends Iterable<C>, C, O> implements Function<R, O>
{
    private final Integer[] columnIndexes;

    private final ReadMapper<O> objectMapper;

    public ContentRowReader(Integer[] columnIndexes, ReadMapper<O> objectMapper)
    {
        this.columnIndexes = columnIndexes;
        this.objectMapper = objectMapper;
    }

    @Override
    public O apply(R row)
    {
        String[] values = new String[columnIndexes.length];

        for (int i = 0; i < columnIndexes.length; i++)
        {
            C cell = getCell(row, columnIndexes[i]);

            // Non-existent cells will be left as null in values
            if (cell != null)
            {
                values[i] = getCellValue(cell);
            }
        }

        return objectMapper.apply(values);
    }

    /**
     * @return null if {@code index} is for a cell that doesn't exist
     */
    @Nullable
    protected abstract C getCell(R row, int index);

    protected abstract String getCellValue(C cell);
}
