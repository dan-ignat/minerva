package name.ignat.minerva.io.read;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static name.ignat.minerva.util.StreamNextRest.streamNextRest;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import name.ignat.minerva.util.StreamNextRest;

/**
 * @param <R> the row type (input)
 * @param <C> the cell type (input)
 * @param <O> the object type (output)
 * 
 * @author Dan Ignat
 */
@RequiredArgsConstructor
public abstract class SpreadsheetReader<R extends Iterable<C>, C> implements AutoCloseable
{
    @Getter
    private final File file;

    public final <O> List<O> read(String[] columnHeaders, Class<O> objectClass)
    {
        Stream<R> rowStream = getRowStream();
        StreamNextRest<R> rowsNextRest = streamNextRest(rowStream);

        HeaderRowReader<R, C> headerRowReader = getHeaderRowReader(columnHeaders);
        Integer[] columnIndexes = rowsNextRest.transformNext(headerRowReader);

        ReadMapper<O> objectMapper = ReadMappers.forClass(objectClass);
        ContentRowReader<R, C, O> contentRowReader = getContentRowReader(columnIndexes, objectMapper);

        Stream<R> contentRowStream = rowsNextRest.getRest();
        return contentRowStream.map(contentRowReader).collect(toImmutableList());
    }

    protected abstract Stream<R> getRowStream();

    protected abstract HeaderRowReader<R, C> getHeaderRowReader(String[] columnHeaders);

    protected abstract <O> ContentRowReader<R, C, O> getContentRowReader(Integer[] columnIndexes, ReadMapper<O> objectMapper);
}
