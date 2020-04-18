package name.ignat.minerva.util;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * @author Dan Ignat
 */
public final class Lists
{
    /**
     * @param list Not null-safe because then you couldn't distinguish between null list and null first element.  Seems
     * consistent with other Collection utilities.
     */
    public static <T> T getFirst(@Nonnull List<T> list)
    {
        return list.get(0);
    }

    /**
     * @param list Not null-safe because then you couldn't distinguish between null list and null last element.  Seems
     * consistent with other Collection utilities.
     */
    public static <T> T getLast(@Nonnull List<T> list)
    {
        return list.get(list.size() - 1);
    }

    public static <T> List<List<T>> arraysToLists(List<T[]> arrays)
    {
        return arrays.stream().map(Arrays::asList).collect(toImmutableList());
    }

    private Lists() { }
}
