package name.ignat.minerva.io.write;

import java.util.Collection;

/**
 * @param <O> the object type (input)
 * @param <S> the sheet type (output)
 * 
 * @author Dan Ignat
 */
public abstract class SpreadsheetWriter<S> implements AutoCloseable
{
    public final <O> void write(String[] columnHeaders, Collection<O> objects, Class<O> objectClass)
    {
        WriteMapper<O> objectMapper = WriteMappers.forClass(objectClass);

        S sheet = getSheet();

        HeaderRowWriter<S> headerRowWriter = getHeaderRowWriter();
        headerRowWriter.accept(columnHeaders, sheet);

        ContentRowWriter<O, S> contentRowWriter = getContentRowWriter(objectMapper, columnHeaders.length);
        objects.stream().forEach(o -> contentRowWriter.accept(o, sheet));

        finishSheet(sheet, columnHeaders.length);
    }

    protected abstract S getSheet();

    protected abstract HeaderRowWriter<S> getHeaderRowWriter();

    protected abstract <O> ContentRowWriter<O, S> getContentRowWriter(WriteMapper<O> objectMapper, int numColumns);

    protected abstract void finishSheet(S sheet, int numColumns);
}
