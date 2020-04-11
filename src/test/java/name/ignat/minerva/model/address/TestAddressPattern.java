package name.ignat.minerva.model.address;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TestAddressPattern
{
    private static Stream<Arguments> constructorCases()
    {
        return Stream.of(
            Arguments.of(null,              NullPointerException.class),
            Arguments.of("",                ValidationException.class),
            Arguments.of("reply",           ValidationException.class),
            Arguments.of("/reply",          ValidationException.class),
            Arguments.of("reply/",          ValidationException.class),
            Arguments.of("/reply/",         null),
            Arguments.of("/repl(?:y|ies)/", null),
            Arguments.of("/repl(? y|ies)/", PatternSyntaxException.class),

            Arguments.of("/RePlY/",         null)
        );
    }

    @ParameterizedTest
    @MethodSource("constructorCases")
    public void constructor(String addressPatternString, Class<? extends Throwable> expectedExceptionClass)
    {
        Executable executable = () -> new AddressPattern(addressPatternString);

        if (expectedExceptionClass == null)
        {
            assertDoesNotThrow(executable);
        }
        else
        {
            assertThrows(expectedExceptionClass, executable);
        }
    }

    private static Stream<Arguments> matchesCases()
    {
        return Stream.of(
            Arguments.of("/replies/",       "hit-reply@b.com", false),
            Arguments.of("/repl(?:y|ies)/", "hit-reply@b.com", true),
            Arguments.of("/RePlY/",         "hit-reply@b.com", true)
        );
    }

    @ParameterizedTest
    @MethodSource("matchesCases")
    public void matches(String addressPatternString, String addressString, boolean expectedResult)
    {
        AddressPattern addressPattern = new AddressPattern(addressPatternString);
        Address address = new Address(addressString);

        boolean result = addressPattern.matches(address);

        assertThat(result, is(expectedResult));
    }

    private static Stream<Arguments> toCanonicalCases()
    {
        return Stream.of(
            Arguments.of("/reply/",   "/reply/"),
            Arguments.of("/RePlY/",   "/RePlY/"),
            // Trimming
            Arguments.of(" /reply/ ", "/reply/")
        );
    }

    @ParameterizedTest
    @MethodSource("toCanonicalCases")
    public void toCanonical(String addressPatternString, String expectedCanonical)
    {
        AddressPattern addressPattern = new AddressPattern(addressPatternString);

        String canonical = addressPattern.toCanonical();

        assertThat(canonical, is(expectedCanonical));
    }

    private static Stream<Arguments> toStringCases()
    {
        return Stream.of(
            Arguments.of("/reply/",   "AddressPattern(/reply/)")
        );
    }

    @ParameterizedTest
    @MethodSource("toStringCases")
    public void toString(String addressPatternString, String expectedString)
    {
        AddressPattern addressPattern = new AddressPattern(addressPatternString);

        String string = addressPattern.toString();

        assertThat(string, is(expectedString));
    }
}
