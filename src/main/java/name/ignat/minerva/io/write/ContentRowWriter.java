package name.ignat.minerva.io.write;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;

import java.util.function.BiConsumer;

/**
 * @param <O> the object type (input)
 * @param <S> the sheet type (output)
 * 
 * @author Dan Ignat
 */
public abstract class ContentRowWriter<O, S> implements BiConsumer<O, S>
{
    private final WriteMapper<O> objectMapper;
    private final int numColumns;

    public ContentRowWriter(WriteMapper<O> objectMapper, int numColumns)
    {
        this.objectMapper = objectMapper;
        this.numColumns = numColumns;
    }

    @Override
    public void accept(O object, S sheet)
    {
        String[] values = objectMapper.apply(object);

        assertThat(values, arrayWithSize(numColumns));

        writeRow(values, sheet);
    }

    protected abstract void writeRow(String[] values, S sheet);
}
