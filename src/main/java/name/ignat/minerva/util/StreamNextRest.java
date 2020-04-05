package name.ignat.minerva.util;

import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.Getter;

/**
 * Allows processing the next element of a {@link Stream} and then the rest of it, similar to languages such as LISP
 * (in which "next/rest" are called "CAR/CDR").
 * 
 * @author Dan Ignat
 */
public class StreamNextRest<T>
{
    public static <T> StreamNextRest<T> streamNextRest(Stream<T> stream)
    {
        return new StreamNextRest<T>(stream);
    }

    private Stream<T> rest;

    public StreamNextRest(Stream<T> stream)
    {
        rest = stream;
    }

    public void consumeNext(Consumer<? super T> consumer)
    {
        consumeNext(consumer, false);
    }

    public void consumeNext(Consumer<? super T> consumer, boolean parallel)
    {
        tryAdvance(consumer, parallel);
    }

    public <R> R transformNext(Function<? super T, R> function)
    {
        return transformNext(function, false);
    }

    public <R> R transformNext(Function<? super T, R> function, boolean parallel)
    {
        FunctionalConsumer<? super T, R> functionalConsumer = new FunctionalConsumer<>(function);

        tryAdvance(functionalConsumer, parallel);

        return functionalConsumer.getResult();
    }

    public boolean testNext(Predicate<? super T> predicate)
    {
        return testNext(predicate, false);
    }

    public boolean testNext(Predicate<? super T> predicate, boolean parallel)
    {
        PredicativeConsumer<? super T> predicativeConsumer = new PredicativeConsumer<>(predicate);

        tryAdvance(predicativeConsumer, parallel);

        return predicativeConsumer.isResult();
    }

    private void tryAdvance(Consumer<? super T> consumer, boolean parallel)
    {
        Spliterator<T> spliterator = rest.spliterator();

        if (spliterator.tryAdvance(consumer))
        {
            rest = StreamSupport.stream(spliterator, parallel);
        }
        else
        {
            throw new NoSuchElementException();
        }
    }

    public Stream<T> getRest()
    {
        return rest;
    }

    /**
     * Adapter that allows a {@link Function} to be used as a {@link Consumer}.
     * <p>
     * Some APIs don't yet support {@code Function}s, for no apparent reason (e.g. {@link Spliterator#tryAdvance(
     * Consumer)}).
     */
    private class FunctionalConsumer<T2, R> implements Consumer<T2>
    {
        private final Function<T2, R> function;

        @Getter
        private R result;

        public FunctionalConsumer(Function<T2, R> function)
        {
            this.function = function;
        }

        @Override
        public void accept(T2 object)
        {
            result = function.apply(object);
        }
    }

    /**
     * Adapter that allows a {@link Predicate} to be used as a {@link Consumer}.
     * <p>
     * Some APIs don't yet support {@code Predicate}s, for no apparent reason (e.g. {@link Spliterator#tryAdvance(
     * Consumer)}).
     */
    private class PredicativeConsumer<T2> implements Consumer<T2>
    {
        private final Predicate<T2> predicate;

        @Getter
        private boolean result;

        public PredicativeConsumer(Predicate<T2> predicate)
        {
            this.predicate = predicate;
        }

        @Override
        public void accept(T2 object)
        {
            result = predicate.test(object);
        }
    }
}
