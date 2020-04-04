package name.ignat.minerva.model;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.zip;
import static java.util.stream.Collectors.toList;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.ADDED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.DUPLICATE;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.EXCLUDED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.FLAGGED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.REMOVED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.REMOVE_NA;
import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.ADDRESS_FILTERS;
import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.NO_FROM_ADDRESS;
import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.NO_RULE_MATCHED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.collect.ImmutableList;

import name.ignat.minerva.model.AddressFilters.AddressMatchers;
import name.ignat.minerva.model.AuditLog.AddressEntry;
import name.ignat.minerva.model.AuditLog.MessageFlag;
import name.ignat.minerva.model.AuditLog.MessageFlag.Reason;
import name.ignat.minerva.rule.Rule;
import name.ignat.minerva.rule.impl.AddSenderRule;
import name.ignat.minerva.rule.impl.RemoveSenderRule;

/**
 * Tests specifically what {@code AddressBook} does when an address matches or doesn't match its filters.
 * <p>
 * More comprehensive tests for matching an address to domains, subdomains, and patterns can be found in {@link
 * TestAddressMatchers}, and are thus not repeated here so as to not duplicate coverage.
 * 
 * @see TestAddressMatchers
 * 
 * @author Dan Ignat
 */
public class TestAddressBook
{
    private static Stream<Arguments> initCases()
    {
        return Stream.of(
            // Basic
            Arguments.of(List.of(),          List.of(),          List.of("a@b.com", "b@b.com"), List.of("a@b.com", "b@b.com"), List.of(ADDED, ADDED)),
            Arguments.of(List.of(),          List.of(),          List.of("a@b.com", "a@b.com"), List.of("a@b.com"),            List.of(ADDED, DUPLICATE)),

            // Exclusions
            Arguments.of(List.of("a@b.com"), List.of(),          List.of("b@b.com"),            List.of("b@b.com"),            List.of(ADDED)),
            Arguments.of(List.of("a@b.com"), List.of(),          List.of("a@b.com"),            List.of(),                     List.of(EXCLUDED)),

            // Flags
            Arguments.of(List.of(),          List.of("a@b.com"), List.of("b@b.com"),            List.of("b@b.com"),            List.of(ADDED)),
            Arguments.of(List.of(),          List.of("a@b.com"), List.of("a@b.com"),            List.of(),                     List.of(FLAGGED)),

            // Exclusions and Flags
            Arguments.of(List.of("a@b.com"), List.of("a@b.com"), List.of("a@b.com"),            List.of(),                     List.of(FLAGGED))
        );
    }

    @ParameterizedTest
    @MethodSource("initCases")
    public void init(List<String> exclusionStrings, List<String> flagStrings, List<String> initialAddressStrings,
        List<String> expectedAddressStrings, List<AddressEntry.Type> expectedAddressEntryTypes)
    {
        AddressBook addressBook = new AddressBook(new AddressFilters(
            AddressMatchers.fromStrings(exclusionStrings), AddressMatchers.fromStrings(flagStrings)));

        // CALL UNDER TEST
        addressBook.init(Address.fromStrings(initialAddressStrings));

        // Assert addresses
        {
            List<Address> addresses = ImmutableList.copyOf(addressBook.getAddresses());
            List<Address> expectedAddresses = Address.fromStrings(expectedAddressStrings);

            assertThat(addresses, is(expectedAddresses));
        }

        // Assert addressEntries
        {
            List<AddressEntry> addressEntries = addressBook.getAuditLog().getAddressEntries();

            List<AddressEntry> expectedAddressEntries =
                zip(
                    initialAddressStrings.stream(),
                    expectedAddressEntryTypes.stream(),
                    (initialAddressString, expectedAddressEntryType) ->
                        new AddressEntry(expectedAddressEntryType, new Address(initialAddressString), null, null))
                .collect(toImmutableList());

            assertThat(addressEntries, is(expectedAddressEntries));
        }
    }

    private static Stream<Arguments> addCases()
    {
        return Stream.of(
            // Basic
            Arguments.of(List.of(),          List.of(),          List.of(),          "b@b.com", List.of("b@b.com"),            ADDED,     null),

            // Initial addresses
            Arguments.of(List.of(),          List.of(),          List.of("a@b.com"), "b@b.com", List.of("a@b.com", "b@b.com"), ADDED,     null),
            Arguments.of(List.of(),          List.of(),          List.of("b@b.com"), "b@b.com", List.of("b@b.com"),            DUPLICATE, null),

            // Exclusions
            Arguments.of(List.of("a@b.com"), List.of(),          List.of(),          "b@b.com", List.of("b@b.com"),            ADDED,     null),
            Arguments.of(List.of("b@b.com"), List.of(),          List.of(),          "b@b.com", List.of(),                     EXCLUDED,  null),

            // Flags
            Arguments.of(List.of(),          List.of("a@b.com"), List.of(),          "b@b.com", List.of("b@b.com"),            ADDED,     null),
            Arguments.of(List.of(),          List.of("b@b.com"), List.of(),          "b@b.com", List.of(),                     FLAGGED,   ADDRESS_FILTERS)
        );
    }

    @ParameterizedTest
    @MethodSource("addCases")
    public void add(
        List<String> exclusionStrings, List<String> flagStrings,
        List<String> initialAddressStrings, String addressToAddString,
        List<String> expectedAddressStrings,
        AddressEntry.Type expectedAddressEntryType, MessageFlag.Reason expectedMessageFlagReason)
    {
        AddressBook addressBook = new AddressBook(new AddressFilters(
            AddressMatchers.fromStrings(exclusionStrings), AddressMatchers.fromStrings(flagStrings)));

        addressBook.init(Address.fromStrings(initialAddressStrings));

        Message sourceMessage = new Message(1, addressToAddString, "Hello", "Lorem ipsum dolor");
        Rule matchedRule = new AddSenderRule();

        // CALL UNDER TEST
        boolean changed = addressBook.add(new Address(addressToAddString), sourceMessage, matchedRule);

        doAddRemoveAsserts(exclusionStrings, flagStrings, initialAddressStrings, addressToAddString,
            sourceMessage, matchedRule, addressBook, changed,
            expectedAddressStrings,
            expectedAddressEntryType, expectedMessageFlagReason);
    }

    private static Stream<Arguments> removeCases()
    {
        return Stream.of(
            // Basic
            Arguments.of(List.of(),          List.of(),          List.of(),          "b@b.com", List.of(),          REMOVE_NA, null),

            // Initial addresses
            Arguments.of(List.of(),          List.of(),          List.of("a@b.com"), "b@b.com", List.of("a@b.com"), REMOVE_NA, null),
            Arguments.of(List.of(),          List.of(),          List.of("b@b.com"), "b@b.com", List.of(),          REMOVED,   null),

            // Non-matching exclusions don't affect removal
            Arguments.of(List.of("c@b.com"), List.of(),          List.of("a@b.com"), "b@b.com", List.of("a@b.com"), REMOVE_NA, null),
            Arguments.of(List.of("c@b.com"), List.of(),          List.of("b@b.com"), "b@b.com", List.of(),          REMOVED,   null),

            // Non-matching flags don't affect removal
            Arguments.of(List.of(),          List.of("c@b.com"), List.of("a@b.com"), "b@b.com", List.of("a@b.com"), REMOVE_NA, null),
            Arguments.of(List.of(),          List.of("c@b.com"), List.of("b@b.com"), "b@b.com", List.of(),          REMOVED,   null)
        );
    }

    @ParameterizedTest
    @MethodSource("removeCases")
    public void remove(
        List<String> exclusionStrings, List<String> flagStrings,
        List<String> initialAddressStrings, String addressToRemoveString,
        List<String> expectedAddressStrings,
        AddressEntry.Type expectedAddressEntryType, MessageFlag.Reason expectedMessageFlagReason)
    {
        AddressBook addressBook = new AddressBook(new AddressFilters(
            AddressMatchers.fromStrings(exclusionStrings), AddressMatchers.fromStrings(flagStrings)));

        addressBook.init(Address.fromStrings(initialAddressStrings));

        Message sourceMessage = new Message(1, addressToRemoveString, "Hello", "Lorem ipsum dolor");
        Rule matchedRule = new RemoveSenderRule();

        // CALL UNDER TEST
        boolean changed = addressBook.remove(new Address(addressToRemoveString), sourceMessage, matchedRule);

        doAddRemoveAsserts(
            exclusionStrings, flagStrings,
            initialAddressStrings, addressToRemoveString,
            sourceMessage, matchedRule, addressBook, changed,
            expectedAddressStrings,
            expectedAddressEntryType, expectedMessageFlagReason);
    }

    private void doAddRemoveAsserts(
        List<String> exclusionStrings, List<String> flagStrings,
        List<String> initialAddressStrings, String addressToTestString,
        Message sourceMessage, Rule matchedRule, AddressBook addressBook, boolean changed,
        List<String> expectedAddressStrings,
        AddressEntry.Type expectedAddressEntryType, MessageFlag.Reason expectedMessageFlagReason)
    {
        // Assert addresses
        {
            List<Address> addresses = ImmutableList.copyOf(addressBook.getAddresses());

            assertThat(addresses, is(Address.fromStrings(expectedAddressStrings)));

            boolean expectedChanged = !expectedAddressStrings.equals(initialAddressStrings);

            assertThat(changed, is(expectedChanged));
        }

        // Assert addressEntries
        {
            List<AddressEntry> addressEntries = addressBook.getAuditLog().getAddressEntries();

            List<AddressEntry> expectedAddressEntries = initialAddressStrings.stream()
                .map(initialAddressString -> new AddressEntry(ADDED, new Address(initialAddressString), null, null))
                .collect(toList());

            expectedAddressEntries.add(new AddressEntry(expectedAddressEntryType, new Address(addressToTestString),
                sourceMessage, matchedRule));

            assertThat(addressEntries, is(expectedAddressEntries));
        }

        // Assert messageFlags
        {
            List<MessageFlag> expectedMessageFlags = expectedMessageFlagReason == null ? List.of() :
                List.of(new MessageFlag(sourceMessage, matchedRule, expectedMessageFlagReason));

            List<MessageFlag> messageFlags = addressBook.getAuditLog().getMessageFlags();

            assertThat(messageFlags, is(expectedMessageFlags));
        }
    }

    private static Stream<Arguments> flagMessageCases()
    {
        return Stream.of(
            Arguments.of(new Message(1, "/O=EXCHANGELABS.../CN=MICROSOFTEXCHANGE...", "Hello", "Lorem ipsum dolor"),
                new AddSenderRule(), NO_FROM_ADDRESS),
            Arguments.of(new Message(1, "a@b.com", "Hello", "Lorem ipsum dolor"), null, NO_RULE_MATCHED)
        );
    }

    @ParameterizedTest
    @MethodSource("flagMessageCases")
    public void flagMessage(Message message, @Nullable Rule matchedRule, Reason reason)
    {
        AddressBook addressBook = new AddressBook();

        List<MessageFlag> expectedMessageFlags = List.of(new MessageFlag(message, matchedRule, reason));

        // CALL UNDER TEST
        addressBook.flagMessage(message, matchedRule, reason);

        List<MessageFlag> messageFlags = addressBook.getAuditLog().getMessageFlags();

        assertThat(messageFlags, is(expectedMessageFlags));
    }
}
