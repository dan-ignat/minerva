package name.ignat.minerva.util;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dan Ignat
 */
public final class Lists
{
    public static <T> List<List<T>> arraysToLists(List<T[]> arrays)
    {
        return arrays.stream().map(Arrays::asList).collect(toImmutableList());
    }

    private Lists() { }
}
