package name.ignat.minerva.model;

import static java.util.Collections.unmodifiableList;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.ADDED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.DUPLICATE;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.EXCLUDED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.FLAGGED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.REMOVED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.REMOVE_NA;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import lombok.Value;
import name.ignat.minerva.model.AuditLog.MessageFlag.Reason;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.rule.Rule;

public class AuditLog
{
    private final List<AddressEntry> addressEntries = new ArrayList<>();

    private final List<MessageFlag> messageFlags = new ArrayList<>();

    public void onAddressAdded(Address address, @Nullable Message sourceMessage, @Nullable Rule matchedRule)
    {
        addressEntries.add(new AddressEntry(ADDED, address, sourceMessage, matchedRule));
    }

    public void onAddressDuplicate(Address address, @Nullable Message sourceMessage, @Nullable Rule matchedRule)
    {
        addressEntries.add(new AddressEntry(DUPLICATE, address, sourceMessage, matchedRule));
    }

    public void onAddressExcluded(Address address, @Nullable Message sourceMessage, @Nullable Rule matchedRule)
    {
        addressEntries.add(new AddressEntry(EXCLUDED, address, sourceMessage, matchedRule));
    }

    public void onAddressFlagged(Address address, @Nullable Message sourceMessage, @Nullable Rule matchedRule)
    {
        addressEntries.add(new AddressEntry(FLAGGED, address, sourceMessage, matchedRule));
    }

    public void onAddressRemoved(Address address, @Nullable Message sourceMessage, @Nullable Rule matchedRule)
    {
        addressEntries.add(new AddressEntry(REMOVED, address, sourceMessage, matchedRule));
    }

    public void onAddressRemoveNotApplicable(Address address, @Nullable Message sourceMessage, @Nullable Rule matchedRule)
    {
        addressEntries.add(new AddressEntry(REMOVE_NA, address, sourceMessage, matchedRule));
    }

    public void onMessageFlagged(Message message, @Nullable Rule matchedRule, Reason reason)
    {
        messageFlags.add(new MessageFlag(message, matchedRule, reason));
    }

    public List<AddressEntry> getAddressEntries()
    {
        return unmodifiableList(addressEntries);
    }

    public List<MessageFlag> getMessageFlags()
    {
        return unmodifiableList(messageFlags);
    }

    @Immutable
    @Value
    public static class AddressEntry
    {
        @Nonnull
        private Type type;

        @Nonnull
        private Address address;

        @Nullable
        private Message sourceMessage;

        @Nullable
        private Rule matchedRule;

        public enum Type { ADDED, DUPLICATE, EXCLUDED, FLAGGED, REMOVED, REMOVE_NA }
    }

    /**
     * Flags a {@link Message} for manual analysis.
     * 
     * @author Dan Ignat
     */
    @Immutable
    @Value
    public static class MessageFlag
    {
        @Nonnull
        private Message message;

        @Nullable
        private Rule matchedRule;

        @Nonnull
        private Reason reason;

        public enum Reason { NO_FROM_ADDRESS, NO_BODY_ADDRESSES, UNEXPECTED_RULE_MATCHED, NO_RULE_MATCHED }
    }
}
