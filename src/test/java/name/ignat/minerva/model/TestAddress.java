package name.ignat.minerva.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import name.ignat.commons.exception.UnexpectedCaseException;
import name.ignat.minerva.model.Addressable.ValidationException;

public class TestAddress
{
    private static Stream<Arguments> extractAllCases()
    {
        return Stream.of(
            // Simple
            Arguments.of(
                "a@b.com  c@b.com",
                List.of("a@b.com", "c@b.com")
            ),
            // Subdomains
            Arguments.of(
                "a@b.co.uk  c@b3.b2.b.com",
                List.of("a@b.co.uk", "c@b3.b2.b.com")
            ),
            // Whitespace and words, and a period after an address
            Arguments.of(
                "  lorem  \t  a@b.com  ipsum  \r\n  c@b.com.  \n  dolor  ",
                List.of("a@b.com", "c@b.com")
            ),
            // This is how some address hyperlinks appear in message bodies
            Arguments.of(
                "a@b.com  <mailto:b@b.com>  c@b.com",
                List.of("a@b.com", "b@b.com", "c@b.com")
            ),
            // Proper HTML address hyperlink
            Arguments.of(
                "a@b.com  <a href=\"mailto:b@b.com\">  c@b.com",
                List.of("a@b.com", "b@b.com", "c@b.com")
            ),
            // Proper usage of TopLevelDomains.isValidAddressable()
            Arguments.of(
                "a@b.com  cid:image005.png@01CA0496.D9F07360  c@b.com",
                List.of("a@b.com", "c@b.com")
            ),
            // Unsubscribe link at bottom of my messages coming back in replies
            Arguments.of(
                "a@b.com  <mailto:b@b.com?body=UNSUBSCRIBE%20ALL&subject=Recruiter%20List%20Removal>  c@b.com",
                List.of("a@b.com", "b@b.com", "c@b.com")
            ),
            // Unsubscribe link
            Arguments.of(
                "a@b.com  <http://blah.d.com/index.php?m=asp&a=optOut&email=b@b.com>  c@b.com",
                List.of("a@b.com", "b@b.com", "c@b.com")
            ),
            // Pipe char as separator, which many people seem to use in their signatures
            Arguments.of(
                "a@b.com  |b@b.com|  c@b.com",
                List.of("a@b.com", "b@b.com", "c@b.com")
            ),
            // URL-encoded pipe char as separator, which can happen if people don't leave a space before it
            Arguments.of(
                "a@b.com  b@b.com%7c  c@b.com",
                List.of("a@b.com", "b@b.com", "c@b.com")
            ),
            /*
             * URL-encoded pipe char as separator, which can happen if people don't leave a space after it.
             * 
             * FIXME: This case is not desirable, but avoiding it isn't trivial.  It only happened once in a real
             * message, when a recruiter put a bar separator ("|") in her signature and didn't include a space before
             * her e-mail address.  Her e-mail client then created a hyperlink out of the address, and converted the "|"
             * to a URL-encoded "%7C".  If I find it happening more often in the future, then I can potentially add
             * something like "%(hex)(hex)" to patterns, or use lookback or something.  But at this point the extra
             * complexity/ugliness isn't worth it, given the fact that this only happened once due to a typo.
             */
            Arguments.of(
                "a@b.com  %7Cb@b.com  c@b.com",
                List.of("a@b.com", "7cb@b.com", "c@b.com")
            ),
            // Upper-case
            Arguments.of(
                "A@B.COM  C@B.COM",
                List.of("a@b.com", "c@b.com")
            )
        );
    }

    @ParameterizedTest
    @MethodSource("extractAllCases")
    public void extractAll(String string, List<String> expectedAddressStrings)
    {
        Set<Address> addresses = Address.extractAll(string);

        Set<Address> expectedAddresses = Address.fromStringsAsSet(expectedAddressStrings);

        assertThat(addresses, is(expectedAddresses));
    }

    private static Stream<Arguments> constructorCases()
    {
        return Stream.of(
            Arguments.of(null,      NullPointerException.class),
            Arguments.of("",        ValidationException.class),
            Arguments.of("a",       ValidationException.class),
            Arguments.of("a@",      ValidationException.class),
            Arguments.of("a@b",     ValidationException.class),
            Arguments.of("a@com",   ValidationException.class),
            Arguments.of("a@.com",  ValidationException.class),
            Arguments.of("@b",      ValidationException.class),
            Arguments.of("@.com",   ValidationException.class),
            Arguments.of("@b.com",  ValidationException.class),
            Arguments.of("a.b.com", ValidationException.class),
            Arguments.of("a@b.com", null),

            Arguments.of("A@B.COM", null)
        );
    }

    @ParameterizedTest
    @MethodSource("constructorCases")
    public void constructor(String addressString, Class<? extends Throwable> expectedExceptionClass)
    {
        if (expectedExceptionClass == null)
        {
            new Address(addressString);
        }
        else
        {
            Assertions.assertThrows(expectedExceptionClass, () -> new Address(addressString));
        }
    }

    private static Stream<Arguments> belongsToCases()
    {
        return Stream.of(
            Arguments.of("a@b.com",     "b.com",     true),
            Arguments.of("a@b.com",     "c.com",     false),
            Arguments.of("a@b.com",     "b.org",     false),
            Arguments.of("a@c.b.com",   "b.com",     true),
            Arguments.of("a@c.b.com",   "c.com",     false),
            Arguments.of("a@c.b.com",   "c.b.com",   true),
            Arguments.of("a@c.b.com",   "b.b.com",   false),
            Arguments.of("a@c.b.com",   "c.c.com",   false),
            Arguments.of("a@d.c.b.com", "c.b.com",   true),
            Arguments.of("a@d.c.b.com", "d.b.com",   false),
            Arguments.of("a@d.c.b.com", "d.c.b.com", true),
            Arguments.of("a@d.c.b.com", "c.c.b.com", false),
            Arguments.of("a@d.c.b.com", "d.d.b.com", false)
        );
    }

    @ParameterizedTest
    @MethodSource("belongsToCases")
    public void belongsTo(String addressString, String domainString, boolean expectedResult)
    {
        Address address = new Address(addressString);
        Domain domain = new Domain(domainString);

        boolean result = address.belongsTo(domain);

        assertThat(result, is(expectedResult));
    }

    private static Stream<Arguments> compareToCases()
    {
        return Stream.of(
            Arguments.of("a@b.com", '=', "a@b.com"),
            Arguments.of("a@b.com", '<', "a@c.com"),
            Arguments.of("a@b.com", '>', "a@a.com"),
            Arguments.of("a@b.com", '<', "a@a.b.com"),

            Arguments.of("a@b.com", '<', "b@b.com"),

            Arguments.of("a@B.com", '=', "A@b.com"),
            Arguments.of("a@B.com", '<', "A@c.com"),
            Arguments.of("a@B.com", '>', "A@a.com")
        );
    }

    @ParameterizedTest
    @MethodSource("compareToCases")
    public void compareTo(String address1String, char expectedResult, String address2String)
    {
        Address address1 = new Address(address1String);
        Address address2 = new Address(address2String);

        int result = address1.compareTo(address2);

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
            Arguments.of("a@b.com", "a@b.com"),

            Arguments.of("A@B.COM", "a@b.com")
        );
    }

    @ParameterizedTest
    @MethodSource("toCanonicalCases")
    public void toCanonical(String addressString, String expectedCanonical)
    {
        Address address = new Address(addressString);

        String canonical = address.toCanonical();

        assertThat(canonical, is(expectedCanonical));
    }
}
