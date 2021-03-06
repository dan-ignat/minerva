package name.ignat.minerva.io.write;

import java.io.File;
import java.util.Collection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @param <O> the object type (input)
 * @param <S> the sheet type (output)
 * 
 * @author Dan Ignat
 */
@RequiredArgsConstructor
public abstract class SpreadsheetWriter<S> implements AutoCloseable
{
    @Getter
    private final File file;

    public final <O> void write(String[] columnHeaders, Collection<O> objects, Class<O> objectClass)
    {
        S sheet = getSheet();

        HeaderRowWriter<S> headerRowWriter = getHeaderRowWriter();
        headerRowWriter.accept(columnHeaders, sheet);

        WriteMapper<O> objectMapper = WriteMappers.forClass(objectClass);
        ContentRowWriter<O, S> contentRowWriter = getContentRowWriter(objectMapper, columnHeaders.length);
        objects.stream().forEach(o -> contentRowWriter.accept(o, sheet));

        finishSheet(sheet, columnHeaders.length);
    }

    protected abstract S getSheet();

    protected abstract HeaderRowWriter<S> getHeaderRowWriter();

    protected abstract <O> ContentRowWriter<O, S> getContentRowWriter(WriteMapper<O> objectMapper, int numColumns);

    protected abstract void finishSheet(S sheet, int numColumns);
}
