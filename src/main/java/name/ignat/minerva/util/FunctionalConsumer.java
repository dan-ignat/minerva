package name.ignat.minerva.util;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

import lombok.Getter;

/**
 * Adapter that allows a {@link Function} to be used as a {@link Consumer}.
 * <p>
 * Some APIs don't yet support {@code Function}s, for no apparent reason (e.g. {@link Spliterator#tryAdvance(
 * Consumer)}).
 * 
 * @author Dan Ignat
 */
public class FunctionalConsumer<T, R> implements Consumer<T>
{
    private final Function<T, R> function;

    @Getter
    private R result;

    public FunctionalConsumer(Function<T, R> function)
    {
        this.function = function;
    }

    @Override
    public void accept(T object)
    {
        result = function.apply(object);
    }
}
