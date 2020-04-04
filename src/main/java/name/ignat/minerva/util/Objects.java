package name.ignat.minerva.util;

import static com.google.common.collect.Streams.stream;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public final class Objects
{
    // This causes conflict with the overloaded version below, as a lambda can match either a Function or Predicate.
    // Probably would need to name each one differently.
    /*public static <T> boolean ifNotNull(T object, Predicate<T> predicate)
    {
        if (object == null)
        {
            return false;
        }
        else
        {
            return predicate.test(object);
        }
    }*/

    public static <T, U> U ifNotNull(T object, Function<T, U> function)
    {
        if (object == null)
        {
            return null;
        }
        else
        {
            return function.apply(object);
        }
    }

    /*public static <T> void ifNotNull(T object, Consumer<T> consumer)
    {
        if (object != null)
        {
            consumer.accept(object);
        }
    }*/

    public static <T> String toLines(Iterable<T> objects)
    {
        return stream(objects).map(T::toString).collect(joining(lineSeparator(), lineSeparator(), ""));
    }

    private Objects() { }
}
