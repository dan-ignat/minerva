package name.ignat.minerva.io.read;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static name.ignat.minerva.util.Streams.forFirst;

import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

/**
 * @param <R> the row type (input)
 * @param <C> the cell type (input)
 * @param <O> the object type (output)
 * 
 * @author Dan Ignat
 */
public abstract class SpreadsheetReader<R extends Iterable<C>, C> implements AutoCloseable
{
    public final <O> List<O> read(String[] columnHeaders, Class<O> objectClass)
    {
        ReadMapper<O> objectMapper = ReadMappers.forClass(objectClass);

        HeaderRowReader<R, C> headerRowReader = getHeaderRowReader(columnHeaders);

        Stream<R> rowStream = getRowStream();
        Pair<Integer[], Stream<R>> pair = forFirst(rowStream, headerRowReader);
        Integer[] columnIndexes = pair.getLeft();
        Stream<R> contentRowStream = pair.getRight();

        ContentRowReader<R, C, O> contentRowReader = getContentRowReader(columnIndexes, objectMapper);

        return contentRowStream.map(contentRowReader).filter(notNull()).collect(toImmutableList());
    }

    protected abstract Stream<R> getRowStream();

    protected abstract HeaderRowReader<R, C> getHeaderRowReader(String[] columnHeaders);

    protected abstract <O> ContentRowReader<R, C, O> getContentRowReader(Integer[] columnIndexes, ReadMapper<O> objectMapper);
}
