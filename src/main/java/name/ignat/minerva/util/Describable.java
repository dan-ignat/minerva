package name.ignat.minerva.util;

import static java.util.stream.Collectors.joining;

import java.util.Collection;

public interface Describable
{
    static String describe(Collection<? extends Describable> describes)
    {
        return describes.stream().map(Describable::describe).collect(joining(", "));
    }

    String describe();
}
