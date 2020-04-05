package name.ignat.minerva.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TestTopLevelDomains
{
    private static Stream<Arguments> hasValidTLDCases()
    {
        return Stream.of(
            Arguments.of("a@b.com",       true),
            Arguments.of("a@b.blah",      false),

            Arguments.of("b.com",         true),
            Arguments.of("b.org",         true),
            Arguments.of("b.net",         true),
            Arguments.of("b.name",        true),
            Arguments.of("b.xn--11b4c3d", true),
            Arguments.of("b.xn--11b4c3e", false),
            Arguments.of("b.zw",          true),
            Arguments.of("b.blah",        false),

            Arguments.of("A@B.COM",       true),
            Arguments.of("A@B.BLAH",      false),
            Arguments.of("B.COM",         true),
            Arguments.of("B.BLAH",        false)
        );
    }

    @ParameterizedTest
    @MethodSource("hasValidTLDCases")
    public void hasValidTLD(String addressableString, boolean expectedResult)
    {
        boolean result = TopLevelDomains.hasValidTLD(addressableString);

        assertThat(result, is(expectedResult));
    }
}
