package name.ignat.minerva.util;

import static name.ignat.minerva.util.Functions.asConsumer;

import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;

public final class Streams
{
    public static <T> Stream<T> forFirst(Stream<T> stream, Consumer<? super T> action)
    {
        return forFirst(stream, action, false);
    }

    public static <T> Stream<T> forFirst(Stream<T> stream, Consumer<? super T> action, boolean parallel)
    {
        Spliterator<T> spliterator = stream.spliterator();

        if (spliterator.tryAdvance(action))
        {
            return StreamSupport.stream(spliterator, parallel);
        }
        else
        {
            throw new NoSuchElementException();
        }
    }

    public static <S, T extends S, R> Pair<R, Stream<T>> forFirst(Stream<T> stream, Function<S, R> function)
    {
        return forFirst(stream, function, false);
    }

    public static <S, T extends S, R> Pair<R, Stream<T>> forFirst(Stream<T> stream, Function<S, R> function,
        boolean parallel)
    {
        Spliterator<T> spliterator = stream.spliterator();

        FunctionalConsumer<S, R> functionalConsumer = asConsumer(function);

        if (spliterator.tryAdvance(functionalConsumer))
        {
            return Pair.of(functionalConsumer.getResult(), StreamSupport.stream(spliterator, parallel));
        }
        else
        {
            throw new NoSuchElementException();
        }
    }

    private Streams() { }
}
