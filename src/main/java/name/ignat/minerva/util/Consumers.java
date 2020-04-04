package name.ignat.minerva.util;

import org.apache.commons.collections4.Closure;

public final class Consumers
{
    public static <T> org.apache.commons.collections4.Closure<T> toApacheClosure(Closure<T> closure)
    {
        return closure::execute;
    }

    public static <T> Closure<T> fromApacheClosure(org.apache.commons.collections4.Closure<T> closure)
    {
        return closure::execute;
    }

    private Consumers() { }
}
