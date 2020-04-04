package name.ignat.minerva.io.read;

import static com.codepoetics.protonpack.StreamUtils.zipWithIndex;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;
import static java.lang.String.format;

import java.util.Map;
import java.util.function.Function;

import name.ignat.minerva.MinervaException;

/**
 * @param <R> the row type (input)
 * @param <C> the cell type (input)
 * 
 * @author Dan Ignat
 */
public abstract class HeaderRowReader<R extends Iterable<C>, C> implements Function<R, Integer[]>
{
    private final String[] desiredColumnHeaders;

    public HeaderRowReader(String[] desiredColumnHeaders)
    {
        this.desiredColumnHeaders = desiredColumnHeaders;
    }

    @Override
    public Integer[] apply(R headerRow)
    {
        Map<String, Integer> headerIndexMap = zipWithIndex(stream(headerRow))
            .collect(toImmutableMap(i -> getCellValue(i.getValue()), i -> (int) i.getIndex()));

        Integer[] desiredColumnIndexes = new Integer[desiredColumnHeaders.length];

        for (int i = 0; i < desiredColumnHeaders.length; i++)
        {
            String desiredColumnHeader = desiredColumnHeaders[i];

            Integer desiredColumnIndex = headerIndexMap.get(desiredColumnHeader);

            if (desiredColumnIndex == null)
            {
                throw new MinervaException(format("No column found with header '%s'", desiredColumnHeader));
            }

            desiredColumnIndexes[i] = desiredColumnIndex;
        }

        return desiredColumnIndexes;
    }

    protected abstract String getCellValue(C headerCell);
}
