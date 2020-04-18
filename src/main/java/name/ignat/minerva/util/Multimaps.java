package name.ignat.minerva.util;

import java.util.stream.Collector;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

/**
 * @author Dan Ignat
 */
public final class Multimaps
{
    public static <K, V> Collector<SetMultimap<K, V>, ImmutableSetMultimap.Builder<K, V>, ImmutableSetMultimap<K, V>>
        combiningSetMultimapsImmutably()
    {
        return Collector.of(
            ImmutableSetMultimap::builder,
            ImmutableSetMultimap.Builder::putAll,
            (builder1, builder2) -> builder1.putAll(builder2.build()),
            ImmutableSetMultimap.Builder::build);
    }

    private Multimaps() { }
}
