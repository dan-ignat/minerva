package name.ignat.minerva.util;

/**
 * @author Dan Ignat
 */
public final class Array
{
    /**
     * Nicer alternative to array constructors.
     * <p>
     * Also allows varargs to be passed to methods that expect an array.  This may happen either when the method is in
     * an external library and wasn't written with varargs, or when the parameter cannot come last for whatever reason,
     * and is therefore forced to be an array.
     * 
     * @see java.util.Arrays#asList(Object...)
     */
    @SafeVarargs
    public static <T> T[] of(T... values)
    {
        return values;
    }

    private Array() { }
}
