package name.ignat.minerva.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TestAddressMatchers
{
    private static Stream<Arguments> matchCases()
    {
        return Stream.of(
            Arguments.of(List.of("a@b.com"),              "b@b.com",         false),
            Arguments.of(List.of("b@b.com"),              "b@b.com",         true),
            Arguments.of(List.of("a.com"),                "a@b.com",         false),
            Arguments.of(List.of("b.com"),                "a@b.com",         true),
            Arguments.of(List.of("c.b.com"),              "a@b.com",         false),
            Arguments.of(List.of("b.com"),                "a@c.b.com",       true),
            Arguments.of(List.of("c.b.com"),              "a@c.b.com",       true),
            Arguments.of(List.of("c.b.com"),              "a@d.c.b.com",     true),
            Arguments.of(List.of("a@b.com", "b@b.com"),   "b@b.com",         true),
            Arguments.of(List.of("/replies/"),            "hit-reply@b.com", false),
            Arguments.of(List.of("/repl(?:y|ies)/"),      "hit-reply@b.com", true),
            Arguments.of(List.of("/RePlY/"),              "hit-reply@b.com", true)
        );
    }

    @ParameterizedTest
    @MethodSource("matchCases")
    public void match(List<String> matcherStrings, String addressToTestString, boolean expectedResult)
    {
        AddressMatchers matchers = AddressMatchers.fromStrings(matcherStrings);

        boolean result = matchers.match(new Address(addressToTestString));

        assertThat(result, is(expectedResult));
    }
}
