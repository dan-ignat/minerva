package name.ignat.minerva.io.write;

import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.ADDED;
import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.NO_RULE_MATCHED;
import static org.hamcrest.Matchers.is;

import java.util.stream.Stream;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import name.ignat.commons.lang.Arrays;
import name.ignat.minerva.model.AuditLog.AddressEntry;
import name.ignat.minerva.model.AuditLog.MessageFlag;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.source.ContactFileSource;

/**
 * @author Dan Ignat
 */
public class TestWriteMappers
{
    private static Stream<Arguments> nullOrBadValueCases()
    {
        return Stream.of(
            Arguments.of(new Message(1, "a@b", "Hey", null), Arrays.of("1", "", "Hey", "")),
            Arguments.of(
                new MessageFlag(new Message(1, "a@b.com", "Hey", "Hi"), null, NO_RULE_MATCHED),
                Arrays.of("1", "", "NO_RULE_MATCHED")),
            Arguments.of(
                new AddressEntry(ADDED, new Address("a@b.com"), new ContactFileSource("Contacts.xlsm", "Main"), null, null),
                Arrays.of("", "a@b.com", "Main", "ADDED", "", ""))
        );
    }

    @ParameterizedTest
    @MethodSource("nullOrBadValueCases")
    public void nullOrBadValue(Object object, String[] expectedStrings)
    {
        @SuppressWarnings("unchecked")
        String[] strings = ((WriteMapper<Object>) WriteMappers.forClass(object.getClass())).apply(object);

        MatcherAssert.assertThat(strings, is(expectedStrings));
    }
}
