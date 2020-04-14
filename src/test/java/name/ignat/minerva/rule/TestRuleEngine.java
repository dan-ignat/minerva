package name.ignat.minerva.rule;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toList;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.ADDED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.DUPLICATE;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.REMOVED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.REMOVE_NA;
import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.NO_BODY_ADDRESSES;
import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.NO_FROM_ADDRESS;
import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.UNEXPECTED_RULE_MATCHED;
import static name.ignat.minerva.model.source.MainMessageFileSource.MessageField.BODY;
import static name.ignat.minerva.model.source.MainMessageFileSource.MessageField.FROM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.collect.ImmutableList;

import lombok.Value;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.AddressFilters;
import name.ignat.minerva.model.AuditLog.AddressEntry;
import name.ignat.minerva.model.AuditLog.AddressEntry.Type;
import name.ignat.minerva.model.AuditLog.MessageFlag;
import name.ignat.minerva.model.AuditLog.MessageFlag.Reason;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.model.TestAddressBook;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.source.ContactFileSource;
import name.ignat.minerva.model.source.MainMessageFileSource;
import name.ignat.minerva.model.source.MainMessageFileSource.MessageField;
import name.ignat.minerva.rule.impl.AddSenderRule;
import name.ignat.minerva.rule.impl.DeliveryFailureRule;
import name.ignat.minerva.rule.impl.NoLongerHereRule;
import name.ignat.minerva.rule.impl.OutOfOfficeRule;
import name.ignat.minerva.rule.impl.RemoveSenderRule;

/**
 * There is no need to test exclusions or flags ({@link AddressFilters}) here, since they've already been fully tested
 * in {@link TestAddressBook}.  So the testing here is focused on the logic specifically in the {@code .rule} package,
 * namely running {@code RuleEngine}s and verifying that the correct addresses get added, removed, etc.
 * 
 * @author Dan Ignat
 */
public class TestRuleEngine
{
    private static Stream<Arguments> runCases()
    {
        return Stream.of(

            /**********************************************************************************************************
             * AutoRepliesRuleEngine
             **********************************************************************************************************/
            // DeliveryFailureRule
            Arguments.of(
                new AutoRepliesRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "/O=EXCHANGELABS.../CN=MICROSOFTEXCHANGE...",
                    "Undeliverable: Rolling off in September",
                    "Delivery has failed to these recipients or distribution lists: a@b.com"
                ),
                List.of(),
                List.of(new AddressEntryTuple(REMOVED, "a@b.com", BODY, new DeliveryFailureRule(null))),
                List.of()
            ),
            // DeliveryFailureRule flags if no body addresses
            Arguments.of(
                new AutoRepliesRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "/O=EXCHANGELABS.../CN=MICROSOFTEXCHANGE...",
                    "Undeliverable: Rolling off in September",
                    "Delivery has failed to some of the recipients or distribution lists"
                ),
                List.of("a@b.com"),
                List.of(),
                List.of(new MessageFlagTuple(new DeliveryFailureRule(null), NO_BODY_ADDRESSES))
            ),
            // NoLongerHereRule
            Arguments.of(
                new AutoRepliesRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "a@b.com",
                    "Automatic reply: Rolling off in September",
                    "Alice is no longer with the company.  Instead, please contact Bob (b@b.com)."
                ),
                List.of("b@b.com"),
                List.of(
                    new AddressEntryTuple(REMOVED, "a@b.com", FROM, new NoLongerHereRule(null)),
                    new AddressEntryTuple(ADDED, "b@b.com", BODY, new NoLongerHereRule(null))),
                List.of()
            ),
            // NoLongerHereRule flags if no from address
            Arguments.of(
                new AutoRepliesRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "BAD ADDRESS",
                    "Automatic reply: Rolling off in September",
                    "Alice is no longer with the company.  Instead, please contact Bob (b@b.com)."
                ),
                List.of("a@b.com", "b@b.com"),
                List.of(new AddressEntryTuple(ADDED, "b@b.com", BODY, new NoLongerHereRule(null))),
                List.of(new MessageFlagTuple(new NoLongerHereRule(null), NO_FROM_ADDRESS))
            ),
            // NoLongerHereRule flags if no body addresses
            Arguments.of(
                new AutoRepliesRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "a@b.com",
                    "Automatic reply: Rolling off in September",
                    "Alice is no longer with the company."
                ),
                List.of(),
                List.of(new AddressEntryTuple(REMOVED, "a@b.com", FROM, new NoLongerHereRule(null))),
                List.of(new MessageFlagTuple(new NoLongerHereRule(null), NO_BODY_ADDRESSES))
            ),
            // NoLongerHereRule flags if no from address and no body addresses
            Arguments.of(
                new AutoRepliesRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "BAD ADDRESS",
                    "Automatic reply: Rolling off in September",
                    "Alice is no longer with the company."
                ),
                List.of("a@b.com"),
                List.of(),
                List.of(
                    new MessageFlagTuple(new NoLongerHereRule(null), NO_FROM_ADDRESS),
                    new MessageFlagTuple(new NoLongerHereRule(null), NO_BODY_ADDRESSES))
            ),
            // NoLongerHereRule matches before OutOfOfficeRule, despite "Out of Office" in subject
            Arguments.of(
                new AutoRepliesRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "a@b.com",
                    "Out of Office: Rolling off in September",
                    "Alice is no longer with the company.  Instead, please contact Bob (b@b.com)."
                ),
                List.of("b@b.com"),
                List.of(
                    new AddressEntryTuple(REMOVED, "a@b.com", FROM, new NoLongerHereRule(null)),
                    new AddressEntryTuple(ADDED, "b@b.com", BODY, new NoLongerHereRule(null))),
                List.of()
            ),
            // OutOfOfficeRule
            Arguments.of(
                new AutoRepliesRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "a@b.com",
                    "Out of Office: Rolling off in September",
                    "Alice is on vacation.  In her absence, you may contact Bob (b@b.com)."
                ),
                List.of("a@b.com", "b@b.com"),
                List.of(new AddressEntryTuple(ADDED, "b@b.com", BODY, new OutOfOfficeRule(null))),
                List.of()
            ),
            // OutOfOfficeRule with keywords only in body
            Arguments.of(
                new AutoRepliesRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "a@b.com",
                    "Automatic reply: Rolling off in September",
                    "I am out of the office.  In my absence, you may contact Bob (b@b.com)."
                ),
                List.of("a@b.com", "b@b.com"),
                List.of(new AddressEntryTuple(ADDED, "b@b.com", BODY, new OutOfOfficeRule(null))),
                List.of()
            ),
            // OutOfOfficeRule doesn't flag if no body addresses
            Arguments.of(
                new AutoRepliesRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "a@b.com",
                    "Out of Office: Rolling off in September",
                    "Alice is on vacation."
                ),
                List.of("a@b.com"),
                List.of(),
                List.of()
            ),

            /**********************************************************************************************************
             * AddSendersRuleEngine
             **********************************************************************************************************/
            // FlagAutoReplyRule - DeliveryFailureRule
            Arguments.of(
                new AddSendersRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "/O=EXCHANGELABS.../CN=MICROSOFTEXCHANGE...",
                    "Undeliverable: Rolling off in September",
                    "Delivery has failed to these recipients or distribution lists: a@b.com"
                ),
                List.of("a@b.com"),
                List.of(),
                List.of(new MessageFlagTuple(new DeliveryFailureRule(null), UNEXPECTED_RULE_MATCHED))
            ),
            // FlagAutoReplyRule - NoLongerHereRule
            Arguments.of(
                new AddSendersRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "a@b.com",
                    "Automatic reply: Rolling off in September",
                    "Alice is no longer with the company.  Instead, please contact Bob (b@b.com)."
                ),
                List.of("a@b.com"),
                List.of(),
                List.of(new MessageFlagTuple(new NoLongerHereRule(null), UNEXPECTED_RULE_MATCHED))
            ),
            // FlagAutoReplyRule - OutOfOfficeRule
            Arguments.of(
                new AddSendersRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "a@b.com",
                    "Out of Office: Rolling off in September",
                    "Alice is on vacation.  In her absence, you may contact Bob (b@b.com)."
                ),
                List.of("a@b.com"),
                List.of(),
                List.of(new MessageFlagTuple(new OutOfOfficeRule(null), UNEXPECTED_RULE_MATCHED))
            ),
            // AddSenderRule
            Arguments.of(
                new AddSendersRuleEngine(null),
                List.of(),
                new Message(1, "a@b.com",
                    "Awesome opportunity",
                    "Hi.  Please let me know if you'd be interested in the JD below.  -Alice (a@b.com)"
                ),
                List.of("a@b.com"),
                List.of(
                    new AddressEntryTuple(ADDED, "a@b.com", FROM, new AddSenderRule(null)),
                    new AddressEntryTuple(DUPLICATE, "a@b.com", BODY, new AddSenderRule(null))),
                List.of()
            ),
            // AddSenderRule with address in body
            Arguments.of(
                new AddSendersRuleEngine(null),
                List.of(),
                new Message(1, "hit-reply@linkedin.com",
                    "Awesome opportunity",
                    "Hi.  Please let me know if you'd be interested in the JD below.  -Alice (a@b.com)"
                ),
                List.of("hit-reply@linkedin.com", "a@b.com"),
                List.of(
                    new AddressEntryTuple(ADDED, "hit-reply@linkedin.com", FROM, new AddSenderRule(null)),
                    new AddressEntryTuple(ADDED, "a@b.com", BODY, new AddSenderRule(null))),
                List.of()
            ),
            // AddSenderRule flags if no from address
            Arguments.of(
                new AddSendersRuleEngine(null),
                List.of(),
                new Message(1, "BAD ADDRESS",
                    "Awesome opportunity",
                    "Hi.  Please let me know if you'd be interested in the JD below.  -Alice (a@b.com)"
                ),
                List.of("a@b.com"),
                List.of(new AddressEntryTuple(ADDED, "a@b.com", BODY, new AddSenderRule(null))),
                List.of(new MessageFlagTuple(new AddSenderRule(null), NO_FROM_ADDRESS))
            ),
            // AddSenderRule doesn't flag if no body addresses
            Arguments.of(
                new AddSendersRuleEngine(null),
                List.of(),
                new Message(1, "a@b.com",
                    "Awesome opportunity",
                    "Hi.  Please let me know if you'd be interested in the JD below."
                ),
                List.of("a@b.com"),
                List.of(new AddressEntryTuple(ADDED, "a@b.com", FROM, new AddSenderRule(null))),
                List.of()
            ),

            /**********************************************************************************************************
             * RemoveSendersRuleEngine
             **********************************************************************************************************/
            // FlagAutoReplyRule - DeliveryFailureRule
            Arguments.of(
                new RemoveSendersRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "/O=EXCHANGELABS.../CN=MICROSOFTEXCHANGE...",
                    "Undeliverable: Rolling off in September",
                    "Delivery has failed to these recipients or distribution lists: a@b.com"
                ),
                List.of("a@b.com"),
                List.of(),
                List.of(new MessageFlagTuple(new DeliveryFailureRule(null), UNEXPECTED_RULE_MATCHED))
            ),
            // FlagAutoReplyRule - NoLongerHereRule
            Arguments.of(
                new RemoveSendersRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "a@b.com",
                    "Automatic reply: Rolling off in September",
                    "Alice is no longer with the company.  Instead, please contact Bob (b@b.com)."
                ),
                List.of("a@b.com"),
                List.of(),
                List.of(new MessageFlagTuple(new NoLongerHereRule(null), UNEXPECTED_RULE_MATCHED))
            ),
            // FlagAutoReplyRule - OutOfOfficeRule
            Arguments.of(
                new RemoveSendersRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "a@b.com",
                    "Out of Office: Rolling off in September",
                    "Alice is on vacation.  In her absence, you may contact Bob (b@b.com)."
                ),
                List.of("a@b.com"),
                List.of(),
                List.of(new MessageFlagTuple(new OutOfOfficeRule(null), UNEXPECTED_RULE_MATCHED))
            ),
            // RemoveSenderRule
            Arguments.of(
                new RemoveSendersRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "a@b.com",
                    "Awesome opportunity",
                    "Hi.  Please let me know if you'd be interested in the JD below.  -Alice (a@b.com)"
                ),
                List.of(),
                List.of(
                    new AddressEntryTuple(REMOVED, "a@b.com", FROM, new RemoveSenderRule(null)),
                    new AddressEntryTuple(REMOVE_NA, "a@b.com", BODY, new RemoveSenderRule(null))),
                List.of()
            ),
            // RemoveSenderRule with address in body
            Arguments.of(
                new RemoveSendersRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "hit-reply@linkedin.com",
                    "Awesome opportunity",
                    "Hi.  Please let me know if you'd be interested in the JD below.  -Alice (a@b.com)"
                ),
                List.of(),
                List.of(
                    new AddressEntryTuple(REMOVE_NA, "hit-reply@linkedin.com", FROM, new RemoveSenderRule(null)),
                    new AddressEntryTuple(REMOVED, "a@b.com", BODY, new RemoveSenderRule(null))),
                List.of()
            ),
            // RemoveSenderRule flags if no from address
            Arguments.of(
                new RemoveSendersRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "BAD ADDRESS",
                    "Awesome opportunity",
                    "Hi.  Please let me know if you'd be interested in the JD below.  -Alice (a@b.com)"
                ),
                List.of(),
                List.of(new AddressEntryTuple(REMOVED, "a@b.com", BODY, new RemoveSenderRule(null))),
                List.of(new MessageFlagTuple(new RemoveSenderRule(null), NO_FROM_ADDRESS))
            ),
            // RemoveSenderRule doesn't flag if no body addresses
            Arguments.of(
                new RemoveSendersRuleEngine(null),
                List.of("a@b.com"),
                new Message(1, "a@b.com",
                    "Awesome opportunity",
                    "Hi.  Please let me know if you'd be interested in the JD below."
                ),
                List.of(),
                List.of(new AddressEntryTuple(REMOVED, "a@b.com", FROM, new RemoveSenderRule(null))),
                List.of()
            )
        );
    }

    @ParameterizedTest
    @MethodSource("runCases")
    public void run(
        RuleEngine ruleEngine,
        List<String> initialAddressStrings,
        Message message,
        List<String> expectedAddressStrings,
        List<AddressEntryTuple> expectedAddressLogTuples,
        List<MessageFlagTuple> expectedMessageFlagTuples)
    {
        AddressBook addressBook = new AddressBook();
        addressBook.addInitial(Address.fromStrings(initialAddressStrings), new ContactFileSource(null, null), false);

        // CALL UNDER TEST
        ruleEngine.run(addressBook, List.of(message));

        // Assert addresses
        {
            List<Address> addresses = ImmutableList.copyOf(addressBook.getAddresses());

            List<Address> expectedAddresses = Address.fromStrings(expectedAddressStrings);

            assertThat(addresses, is(expectedAddresses));
        }

        // Assert addressEntries
        {
            List<AddressEntry> addressEntries = addressBook.getAuditLog().getAddressEntries();

            List<AddressEntry> expectedAddressEntries = initialAddressStrings.stream()
                .map(a -> new AddressEntry(ADDED, new Address(a), new ContactFileSource(null, null), null, null))
                .collect(toList());

            expectedAddressEntries.addAll(expectedAddressLogTuples.stream()
                .map(t -> new AddressEntry(t.getType(), new Address(t.getAddressString()),
                    new MainMessageFileSource(null, message, t.getMessageField()), null, t.getMatchedRule()))
                .collect(toImmutableList()));

            assertThat(addressEntries, is(expectedAddressEntries));
        }

        // Assert messageFlags
        {
            List<MessageFlag> messageFlags = addressBook.getAuditLog().getMessageFlags();

            List<MessageFlag> expectedMessageFlags = expectedMessageFlagTuples.stream()
                .map(t -> new MessageFlag(message, t.getMatchedRule(), t.getReason())).collect(toImmutableList());

            assertThat(messageFlags, is(expectedMessageFlags));
        }
    }

    @Value
    private static class AddressEntryTuple
    {
        private Type type;
        private String addressString;
        private MessageField messageField;
        private Rule matchedRule;
    }

    @Value
    private static class MessageFlagTuple
    {
        private Rule matchedRule;
        private Reason reason;
    }
}
