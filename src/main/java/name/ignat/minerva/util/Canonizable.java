package name.ignat.minerva.util;

import static java.util.stream.Collectors.joining;

import java.util.Collection;

public interface Canonizable
{
    static String toCanonical(Collection<? extends Canonizable> canonizables)
    {
        return canonizables.stream().map(Canonizable::toCanonical).collect(joining(", "));
    }

    String toCanonical();
}
