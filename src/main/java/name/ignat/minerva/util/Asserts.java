package name.ignat.minerva.util;

/*import static com.google.common.collect.Streams.forEachPair;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.function.Function;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * @author Dan Ignat
 * /
public final class Asserts
{
    /**
     * Asserts that two insertion-ordered {@code Collection}s are equal, using Hamcrest {@link Matchers} at the item
     * level, which gives a more specific description on failures.
     * <p>
     * Also useful when asserting equality on an entire collection isn't an option.  E.g. for {@code
     * Collection<String[]>}, because an array doesn't have its own {@code equals()} implementation (unexpectedly), and
     * thus this would always fail if the corresponding arrays are different instances (which they usually are).
     * /
    public static <T> void assertItemsEqual(Collection<T> actualCollection, Collection<T> expectedCollection)
    {
        assertItemsMatch(actualCollection, expectedCollection, Matchers::is);
    }

    public static <T> void assertItemsMatch(Collection<T> actualCollection, Collection<T> expectedCollection,
        Function<T, Matcher<T>> matcherGenerator)
    {
        //assertThat(actualCollection, hasSize(expectedCollection.size()));
        if (actualCollection.size() != expectedCollection.size())
        {
            assertThat(actualCollection, is(expectedCollection));
        }

        forEachPair(actualCollection.stream(), expectedCollection.stream(),
            (actualItem, expectedItem) -> assertThat(actualItem, matcherGenerator.apply(expectedItem)));
    }

    private Asserts() { }
}*/
