package name.ignat.minerva.util;

import java.util.function.Predicate;

public final class Predicates
{
    public static <T> org.apache.commons.collections4.Predicate<T> toApachePredicate(Predicate<T> predicate)
    {
        return predicate::test;
    }

    public static <T> Predicate<T> fromApachePredicate(org.apache.commons.collections4.Predicate<T> predicate)
    {
        return predicate::evaluate;
    }

    private Predicates() { }
}
