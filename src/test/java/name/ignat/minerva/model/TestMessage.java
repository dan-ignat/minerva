package name.ignat.minerva.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.collect.ImmutableSet;

import name.ignat.minerva.model.address.Address;

/**
 * @author Dan Ignat
 */
public class TestMessage
{
    private static Stream<Arguments> constructorExceptionCases()
    {
        return Stream.of(
            Arguments.of(1,    "a@b.com", "Hey", "Yo", null),
            Arguments.of(null, "a@b.com", "Hey", "Yo", NullPointerException.class),
            Arguments.of(1,    null,      "Hey", "Yo", NullPointerException.class),
            Arguments.of(1,    "a@b.com", null,  "Yo", NullPointerException.class),
            // Null body is okay
            Arguments.of(1,    "a@b.com", "Hey", null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("constructorExceptionCases")
    public void constructorException(Integer index, String from, String subject, String body,
        Class<? extends Throwable> expectedExceptionClass)
    {
        Executable executable = () -> new Message(index, from, subject, body);

        if (expectedExceptionClass == null)
        {
            assertDoesNotThrow(executable);
        }
        else
        {
            assertThrows(expectedExceptionClass, executable);
        }
    }

    private static Stream<Arguments> constructorCases()
    {
        return Stream.of(
            // Normal
            Arguments.of(new Message(2, "a@b.com", "Hello", "Lorem ipsum dolor"),
                2, "a@b.com", null, "Hello", "Lorem ipsum dolor", List.of()),

            // Body addresses
            Arguments.of(new Message(2, "a@b.com", "Hello", "Lorem b@b.com ipsum c@b.com dolor"),
                2, "a@b.com", null, "Hello", "Lorem b@b.com ipsum c@b.com dolor", List.of("b@b.com", "c@b.com")),

            // Bad from
            Arguments.of(new Message(2, "/O=EXCHANGELABS.../CN=MICROSOFTEXCHANGE...", "Hello", "Lorem ipsum dolor"),
                2, null, "/O=EXCHANGELABS.../CN=MICROSOFTEXCHANGE...", "Hello", "Lorem ipsum dolor", List.of()),

            // Trimming
            Arguments.of(new Message(2, " a@b.com ", " Hello ", " Lorem ipsum dolor "),
                2, "a@b.com", null, " Hello ", " Lorem ipsum dolor ", List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("constructorCases")
    public void constructor(Message message, Integer expectedIndex, String expectedFrom, String expectedFromRaw,
        String expectedSubject, String expectedBody, List<String> expectedBodyAddressStrings)
    {
        assertThat(message.getIndex(), expectedIndex == null ? nullValue() : is(expectedIndex));

        if (expectedFrom == null)
        {
            assertThat(message.getFrom(), nullValue());
        }
        else
        {
            assertThat(message.getFrom().toCanonical(), is(expectedFrom));
        }

        assertThat(message.getFromRaw(), expectedFromRaw == null ? nullValue() : is(expectedFromRaw));

        assertThat(message.getSubject(), expectedSubject == null ? nullValue() : is(expectedSubject));

        assertThat(message.getBody(), expectedBody == null ? nullValue() : is(expectedBody));

        Set<Address> expectedBodyAddresses = ImmutableSet.copyOf(Address.fromStrings(expectedBodyAddressStrings));
        assertThat(message.getBodyAddresses(), is(expectedBodyAddresses));
    }
}
