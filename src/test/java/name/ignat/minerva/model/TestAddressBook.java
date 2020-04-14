package name.ignat.minerva.model;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.zip;
import static java.util.stream.Collectors.toList;
import static name.ignat.commons.utils.ObjectUtils.equalsAny;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.ADDED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.DUPLICATE;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.EXCLUDED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.FLAGGED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.REMOVED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.REMOVE_NA;
import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.NO_FROM_ADDRESS;
import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.NO_RULE_MATCHED;
import static name.ignat.minerva.model.source.MainMessageFileSource.MessageField.FROM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import name.ignat.minerva.model.AuditLog.AddressEntry;
import name.ignat.minerva.model.AuditLog.MessageFlag;
import name.ignat.minerva.model.AuditLog.MessageFlag.Reason;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.address.AddressMatchersUtils;
import name.ignat.minerva.model.address.TestAddressMatchers;
import name.ignat.minerva.model.source.AddressMatcherSource;
import name.ignat.minerva.model.source.AddressSource;
import name.ignat.minerva.model.source.DummyAddressMatcherSource;
import name.ignat.minerva.model.source.DummyAddressSource;
import name.ignat.minerva.model.source.MainMessageFileSource;
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
    private static Stream<Arguments> addInitialCases()
    {
        return Stream.of(
            // Basic
            Arguments.of(List.of(),          List.of(),          List.of("a@b.com", "b@b.com"), false, List.of("a@b.com", "b@b.com"), List.of(ADDED,    ADDED)),
            Arguments.of(List.of(),          List.of(),          List.of("a@b.com", "a@b.com"), false, List.of("a@b.com"),            List.of(ADDED,    DUPLICATE)),

            // Exclusions
            Arguments.of(List.of("a@b.com"), List.of(),          List.of("a@b.com", "b@b.com"), false, List.of("a@b.com", "b@b.com"), List.of(ADDED,    ADDED)),
            Arguments.of(List.of("a@b.com"), List.of(),          List.of("a@b.com", "b@b.com"), true,  List.of("b@b.com"),            List.of(EXCLUDED, ADDED)),

            // Flags
            Arguments.of(List.of(),          List.of("a@b.com"), List.of("a@b.com", "b@b.com"), false, List.of("a@b.com", "b@b.com"), List.of(ADDED,    ADDED)),
            Arguments.of(List.of(),          List.of("a@b.com"), List.of("a@b.com", "b@b.com"), true,  List.of("b@b.com"),            List.of(FLAGGED,  ADDED)),

            // Exclusions and Flags
            Arguments.of(List.of("a@b.com"), List.of("a@b.com"), List.of("a@b.com", "a@b.com"), false, List.of("a@b.com"),            List.of(ADDED,    DUPLICATE)),
            Arguments.of(List.of("a@b.com"), List.of("a@b.com"), List.of("a@b.com", "a@b.com"), true,  List.of(),                     List.of(EXCLUDED, EXCLUDED)),
            Arguments.of(List.of("a@b.com"), List.of("b@b.com"), List.of("b@b.com", "b@b.com"), true,  List.of(),                     List.of(FLAGGED,  FLAGGED))
        );
    }

    @ParameterizedTest
    @MethodSource("addInitialCases")
    public void addInitial(List<String> exclusionStrings, List<String> flagStrings,
        List<String> initialAddressStrings, boolean filterInitialAddresses,
        List<String> expectedAddressStrings, List<AddressEntry.Type> expectedAddressEntryTypes)
    {
        AddressBook addressBook = new AddressBook(new AddressFilters(
            AddressMatchersUtils.fromStrings(exclusionStrings), AddressMatchersUtils.fromStrings(flagStrings)));

        // CALL UNDER TEST
        addressBook.addInitial(Address.fromStrings(initialAddressStrings), new DummyAddressSource(), filterInitialAddresses);

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
                        new AddressEntry(expectedAddressEntryType, new Address(initialAddressString),
                            new DummyAddressSource(), getExpectedFilterSources(expectedAddressEntryType), null))
                .collect(toImmutableList());

            assertThat(addressEntries, is(expectedAddressEntries));
        }
    }

    private static ImmutableSet<AddressMatcherSource> getExpectedFilterSources(
        AddressEntry.Type expectedAddressEntryType)
    {
        return equalsAny(expectedAddressEntryType, EXCLUDED, FLAGGED) ?
            ImmutableSet.of(new DummyAddressMatcherSource()) : null;
    }

    private static Stream<Arguments> addCases()
    {
        return Stream.of(
            // Basic
            Arguments.of(List.of(),          List.of(),          List.of(),          "b@b.com", List.of("b@b.com"),            ADDED),

            // Initial addresses
            Arguments.of(List.of(),          List.of(),          List.of("a@b.com"), "b@b.com", List.of("a@b.com", "b@b.com"), ADDED),
            Arguments.of(List.of(),          List.of(),          List.of("b@b.com"), "b@b.com", List.of("b@b.com"),            DUPLICATE),

            // Exclusions
            Arguments.of(List.of("a@b.com"), List.of(),          List.of(),          "b@b.com", List.of("b@b.com"),            ADDED),
            Arguments.of(List.of("b@b.com"), List.of(),          List.of(),          "b@b.com", List.of(),                     EXCLUDED),

            // Flags
            Arguments.of(List.of(),          List.of("a@b.com"), List.of(),          "b@b.com", List.of("b@b.com"),            ADDED),
            Arguments.of(List.of(),          List.of("b@b.com"), List.of(),          "b@b.com", List.of(),                     FLAGGED),

            // Exclusions and Flags
            Arguments.of(List.of("a@b.com"), List.of("a@b.com"), List.of("a@b.com"), "a@b.com", List.of("a@b.com"),            DUPLICATE),
            Arguments.of(List.of("a@b.com"), List.of("a@b.com"), List.of(),          "a@b.com", List.of(),                     EXCLUDED)
        );
    }

    @ParameterizedTest
    @MethodSource("addCases")
    public void add(
        List<String> exclusionStrings, List<String> flagStrings,
        List<String> initialAddressStrings, String addressToAddString,
        List<String> expectedAddressStrings, AddressEntry.Type expectedAddressEntryType)
    {
        AddressBook addressBook = new AddressBook(new AddressFilters(
            AddressMatchersUtils.fromStrings(exclusionStrings), AddressMatchersUtils.fromStrings(flagStrings)));

        addressBook.addInitial(Address.fromStrings(initialAddressStrings), new DummyAddressSource(), false);

        AddressSource addressSource =
            new MainMessageFileSource(null, new Message(1, addressToAddString, "Hello", "Lorem ipsum dolor"), FROM);

        Rule matchedRule = new AddSenderRule(null);

        // CALL UNDER TEST
        boolean changed = addressBook.add(new Address(addressToAddString), addressSource, matchedRule);

        doAddRemoveAsserts(exclusionStrings, flagStrings, initialAddressStrings, addressToAddString,
            addressSource, matchedRule, addressBook, changed, expectedAddressStrings, expectedAddressEntryType);
    }

    private static Stream<Arguments> removeCases()
    {
        return Stream.of(
            // Basic
            Arguments.of(List.of(),          List.of(),          List.of(),          "b@b.com", List.of(),          REMOVE_NA),

            // Initial addresses
            Arguments.of(List.of(),          List.of(),          List.of("a@b.com"), "b@b.com", List.of("a@b.com"), REMOVE_NA),
            Arguments.of(List.of(),          List.of(),          List.of("b@b.com"), "b@b.com", List.of(),          REMOVED),

            // Exclusions don't affect removal
            Arguments.of(List.of("a@b.com"), List.of(),          List.of("a@b.com"), "b@b.com", List.of("a@b.com"), REMOVE_NA),
            Arguments.of(List.of("a@b.com"), List.of(),          List.of("a@b.com"), "a@b.com", List.of(),          REMOVED),

            // Flags don't affect removal
            Arguments.of(List.of(),          List.of("a@b.com"), List.of("a@b.com"), "b@b.com", List.of("a@b.com"), REMOVE_NA),
            Arguments.of(List.of(),          List.of("a@b.com"), List.of("a@b.com"), "a@b.com", List.of(),          REMOVED)
        );
    }

    @ParameterizedTest
    @MethodSource("removeCases")
    public void remove(
        List<String> exclusionStrings, List<String> flagStrings,
        List<String> initialAddressStrings, String addressToRemoveString,
        List<String> expectedAddressStrings, AddressEntry.Type expectedAddressEntryType)
    {
        AddressBook addressBook = new AddressBook(new AddressFilters(
            AddressMatchersUtils.fromStrings(exclusionStrings), AddressMatchersUtils.fromStrings(flagStrings)));

        addressBook.addInitial(Address.fromStrings(initialAddressStrings), new DummyAddressSource(), false);

        AddressSource addressSource =
            new MainMessageFileSource(null, new Message(1, addressToRemoveString, "Hello", "Lorem ipsum dolor"), FROM);

        Rule matchedRule = new RemoveSenderRule(null);

        // CALL UNDER TEST
        boolean changed = addressBook.remove(new Address(addressToRemoveString), addressSource, matchedRule);

        doAddRemoveAsserts(exclusionStrings, flagStrings, initialAddressStrings, addressToRemoveString,
            addressSource, matchedRule, addressBook, changed, expectedAddressStrings, expectedAddressEntryType);
    }

    private void doAddRemoveAsserts(
        List<String> exclusionStrings, List<String> flagStrings,
        List<String> initialAddressStrings, String addressToTestString,
        AddressSource addressSource, Rule matchedRule, AddressBook addressBook, boolean changed,
        List<String> expectedAddressStrings, AddressEntry.Type expectedAddressEntryType)
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
                .map(initialAddressString ->
                    new AddressEntry(ADDED, new Address(initialAddressString), new DummyAddressSource(), null, null))
                .collect(toList());

            expectedAddressEntries.add(new AddressEntry(expectedAddressEntryType, new Address(addressToTestString),
                addressSource, getExpectedFilterSources(expectedAddressEntryType), matchedRule));

            assertThat(addressEntries, is(expectedAddressEntries));
        }

        // Assert messageFlags
        {
            List<MessageFlag> expectedMessageFlags = List.of();

            List<MessageFlag> messageFlags = addressBook.getAuditLog().getMessageFlags();

            assertThat(messageFlags, is(expectedMessageFlags));
        }
    }

    private static Stream<Arguments> flagMessageCases()
    {
        return Stream.of(
            Arguments.of(new Message(1, "/O=EXCHANGELABS.../CN=MICROSOFTEXCHANGE...", "Hello", "Lorem ipsum dolor"),
                new AddSenderRule(null), NO_FROM_ADDRESS),
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
