package name.ignat.minerva.model;

import static java.util.Collections.unmodifiableCollection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

import lombok.Getter;
import name.ignat.minerva.model.AuditLog.MessageFlag.Reason;
import name.ignat.minerva.rule.Rule;

public class AddressBook
{
    public static Builder builder()
    {
        return new Builder();
    }

    // TODO: Optimize this with expected sizes based on actual incoming data
    private final SetMultimap<Domain, Address> addressesByDomain = LinkedHashMultimap.create(500, 50);

    @Nonnull
    private final AddressFilters addressFilters;

    @Getter
    private final AuditLog auditLog = new AuditLog();

    public AddressBook()
    {
        this(new AddressFilters());
    }

    public AddressBook(AddressFilters addressFilters)
    {
        this.addressFilters = addressFilters;
    }

    public boolean add(@Nonnull Address address, boolean filter)
    {
        return add(address, null, null, filter);
    }

    public boolean add(@Nonnull Address address, @Nullable Message sourceMessage, @Nullable Rule matchedRule)
    {
        return add(address, sourceMessage, matchedRule, true);
    }

    /*
     * Filter checks are done in order of increasing severity (DUPLICATE, EXCLUDED, FLAGGED), to minimize how many
     * high-severity filter actions must be manually analyzed in the output file.
     */
    private boolean add(@Nonnull Address address, @Nullable Message sourceMessage, @Nullable Rule matchedRule,
        boolean filter)
    {
        if (addressesByDomain.containsEntry(address.getDomain(), address))
        {
            auditLog.onAddressDuplicate(address, sourceMessage, matchedRule);

            return false;
        }
        else if (filter && addressFilters.shouldExclude(address))
        {
            auditLog.onAddressExcluded(address, sourceMessage, matchedRule);

            return false;
        }
        else if (filter && addressFilters.shouldFlag(address))
        {
            auditLog.onAddressFlagged(address, sourceMessage, matchedRule);

            return false;
        }
        else
        {
            boolean changed = addressesByDomain.put(address.getDomain(), address);

            assertThat(changed, is(true));

            auditLog.onAddressAdded(address, sourceMessage, matchedRule);

            return true;
        }
    }

    public void init(@Nonnull Collection<Address> addresses, boolean filter)
    {
        addresses.stream().forEach(address -> add(address, filter));
    }

    public boolean remove(@Nonnull Address address)
    {
        return remove(address, null, null);
    }

    public boolean remove(@Nonnull Address address, @Nullable Message sourceMessage, @Nullable Rule matchedRule)
    {
        boolean changed = addressesByDomain.remove(address.getDomain(), address);

        if (changed)
        {
            auditLog.onAddressRemoved(address, sourceMessage, matchedRule);
        }
        else
        {
            auditLog.onAddressRemoveNotApplicable(address, sourceMessage, matchedRule);
        }

        return changed;
    }

    /**
     * Flags {@code message} for manual analysis.
     */
    public void flagMessage(@Nonnull Message message, @Nullable Rule matchedRule, @Nonnull Reason reason)
    {
        auditLog.onMessageFlagged(message, matchedRule, reason);
    }

    /*
     * This could be made a List or Set.  Technically it could be a Set, since AddressBook.add() guarantees that there
     * can be no duplicates in the SetMultimap values.  But then we'd have to return a copy of the addresses that
     * doesn't update automatically, and that would be inconsistent with AuditLog getters.  Or we can change them all to
     * be copies, but there doesn't seem to be a great reason for it.
     * 
     * If you need to compare this returned Collection with another via equals(), first convert it to a List or Set,
     * since Collections.unmodifiableCollection() specifically mentions that it doesn't pass equals/hashCode on to the
     * underlying Collection, but rather uses Object's, so it will fail if it's not the same instance.
     */
    public Collection<Address> getAddresses()
    {
        return unmodifiableCollection(addressesByDomain.values());
    }

    public static class Builder
    {
        private List<String> exclusionAddressStrings = List.of();
        private List<String> exclusionDomainStrings = List.of();
        private List<String> exclusionPatternStrings = List.of();

        private List<String> flagAddressStrings = List.of();
        private List<String> flagDomainStrings = List.of();
        private List<String> flagPatternStrings = List.of();

        private List<String> initialAddressStrings = List.of();

        private Builder() { }

        public Builder exclusionAddresses(List<String> exclusionAddressStrings)
        {
            this.exclusionAddressStrings = exclusionAddressStrings;
            return this;
        }

        public Builder exclusionDomains(List<String> exclusionDomainStrings)
        {
            this.exclusionDomainStrings = exclusionDomainStrings;
            return this;
        }

        public Builder exclusionPatterns(List<String> exclusionPatternStrings)
        {
            this.exclusionPatternStrings = exclusionPatternStrings;
            return this;
        }

        public Builder flagAddresses(List<String> flagAddressStrings)
        {
            this.flagAddressStrings = flagAddressStrings;
            return this;
        }

        public Builder flagDomains(List<String> flagDomainStrings)
        {
            this.flagDomainStrings = flagDomainStrings;
            return this;
        }

        public Builder flagPatterns(List<String> flagPatternStrings)
        {
            this.flagPatternStrings = flagPatternStrings;
            return this;
        }

        public Builder initialAddresses(List<String> initialAddressStrings)
        {
            this.initialAddressStrings = initialAddressStrings;
            return this;
        }

        public AddressBook build()
        {
            AddressBook addressBook = new AddressBook(new AddressFilters(
                Address.fromStrings(exclusionAddressStrings), Domain.fromStrings(exclusionDomainStrings),
                AddressPattern.fromStrings(exclusionPatternStrings),
                Address.fromStrings(flagAddressStrings), Domain.fromStrings(flagDomainStrings),
                AddressPattern.fromStrings(flagPatternStrings)));

            addressBook.init(Address.fromStrings(initialAddressStrings), false);

            return addressBook;
        }
    }
}
