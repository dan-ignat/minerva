package name.ignat.minerva.util;

import java.util.function.Function;

import org.apache.commons.collections4.Transformer;

/**
 * @author Dan Ignat
 */
public final class Functions
{
    public static <T, R> org.apache.commons.collections4.Transformer<T, R> toApacheTransformer(Function<T, R> function)
    {
        return function::apply;
    }

    public static <T, R> Transformer<T, R> fromApacheTransformer(
        org.apache.commons.collections4.Transformer<T, R> transformer)
    {
        return transformer::transform;
    }

    private Functions() { }
}
