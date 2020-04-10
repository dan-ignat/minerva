package name.ignat.minerva.model.address;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import name.ignat.commons.exception.UnexpectedCaseException;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.address.Domain;
import name.ignat.minerva.model.address.ValidationException;

public class TestDomain
{
    private static Stream<Arguments> constructorCases()
    {
        return Stream.of(
            Arguments.of(null,          NullPointerException.class),
            Arguments.of("",            ValidationException.class),
            Arguments.of("b",           ValidationException.class),
            Arguments.of("com",         ValidationException.class),
            Arguments.of(".com",        ValidationException.class),
            Arguments.of("b.com",       null),
            Arguments.of(".b.com",      ValidationException.class),
            Arguments.of("c3.b2.b.com", null),

            Arguments.of("B.COM",       null)
        );
    }

    @ParameterizedTest
    @MethodSource("constructorCases")
    public void constructor(String domainString, Class<? extends Throwable> expectedExceptionClass)
    {
        Executable executable = () -> new Domain(domainString);

        if (expectedExceptionClass == null)
        {
            assertDoesNotThrow(executable);
        }
        else
        {
            assertThrows(expectedExceptionClass, executable);
        }
    }

    private static Stream<Arguments> isSubdomainOfCases()
    {
        return Stream.of(
            Arguments.of("b.com",    "b.com", true),
            Arguments.of("b.com",    "c.com", false),
            Arguments.of("b.com",    "b.org", false),
            Arguments.of("a.b.com",  "b.com", true),
            Arguments.of("ab.com",   "b.com", false),
            Arguments.of("a.bb.com", "b.com", false)
        );
    }

    @ParameterizedTest
    @MethodSource("isSubdomainOfCases")
    public void isSubdomainOf(String domain1String, String domain2String, boolean expectedResult)
    {
        Domain domain1 = new Domain(domain1String);
        Domain domain2 = new Domain(domain2String);

        boolean result = domain1.isSubdomainOf(domain2);

        assertThat(result, is(expectedResult));
    }

    private static Stream<Arguments> matchesCases()
    {
        return Stream.of(
            Arguments.of("b.com",     "a@b.com",     true),
            Arguments.of("c.com",     "a@b.com",     false),
            Arguments.of("b.org",     "a@b.com",     false),
            Arguments.of("b.com",     "a@c.b.com",   true),
            Arguments.of("c.com",     "a@c.b.com",   false),
            Arguments.of("c.b.com",   "a@c.b.com",   true),
            Arguments.of("b.b.com",   "a@c.b.com",   false),
            Arguments.of("c.c.com",   "a@c.b.com",   false),
            Arguments.of("c.b.org",   "a@c.b.com",   false),
            Arguments.of("c.b.com",   "a@d.c.b.com", true),
            Arguments.of("d.b.com",   "a@d.c.b.com", false),
            Arguments.of("d.c.b.com", "a@d.c.b.com", true),
            Arguments.of("c.c.b.com", "a@d.c.b.com", false),
            Arguments.of("d.d.b.com", "a@d.c.b.com", false),
            Arguments.of("d.c.b.org", "a@d.c.b.com", false)
        );
    }

    @ParameterizedTest
    @MethodSource("matchesCases")
    public void matches(String domainString, String addressString, boolean expectedResult)
    {
        Domain domain = new Domain(domainString);
        Address address = new Address(addressString);

        boolean result = domain.matches(address);

        assertThat(result, is(expectedResult));
    }

    private static Stream<Arguments> compareToCases()
    {
        return Stream.of(
            Arguments.of("b.com", '=', "b.com"),
            Arguments.of("b.com", '<', "c.com"),
            Arguments.of("b.com", '>', "a.com"),
            Arguments.of("b.com", '<', "a.b.com"),

            // Test that 2LD is sorted before TLD
            Arguments.of("b.com", '<', "b.org"),
            Arguments.of("b.us",  '>', "b.org"),
            Arguments.of("b.com", '>', "a.org"),

            Arguments.of("B.com", '=', "b.com"),
            Arguments.of("B.com", '<', "c.com"),
            Arguments.of("B.com", '>', "a.com")
        );
    }

    @ParameterizedTest
    @MethodSource("compareToCases")
    public void compareTo(String domain1String, char expectedResult, String domain2String)
    {
        Domain domain1 = new Domain(domain1String);
        Domain domain2 = new Domain(domain2String);

        int result = domain1.compareTo(domain2);

        switch (expectedResult)
        {
            case '=':
                assertThat(result, is(0));
                break;
            case '<':
                assertThat(result, lessThan(0));
                break;
            case '>':
                assertThat(result, greaterThan(0));
                break;
            default:
                throw new UnexpectedCaseException(expectedResult);
        }
    }

    private static Stream<Arguments> toCanonicalCases()
    {
        return Stream.of(
            Arguments.of("b.com",   "b.com"),
            Arguments.of("B.COM",   "b.com"),
            // Trimming
            Arguments.of(" b.com ", "b.com")
        );
    }

    @ParameterizedTest
    @MethodSource("toCanonicalCases")
    public void toCanonical(String domainString, String expectedCanonical)
    {
        Domain domain = new Domain(domainString);

        String canonical = domain.toCanonical();

        assertThat(canonical, is(expectedCanonical));
    }
}
