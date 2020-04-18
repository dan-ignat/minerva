package name.ignat.minerva.util;

/**
 * @author Dan Ignat
 */
public final class LombokUtils
{
    public static String toCustomString(Canonizable canonizable)
    {
        return toCustomString(canonizable, canonizable.toCanonical());
    }

    public static String toCustomString(Object object, String customContent)
    {
        return object.getClass().getSimpleName() + "(" + customContent + ")";
    }

    private LombokUtils() { }
}
