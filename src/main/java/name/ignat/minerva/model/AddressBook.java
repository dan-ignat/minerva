package name.ignat.minerva.model;

import static java.util.Collections.unmodifiableCollection;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

import lombok.Getter;
import lombok.Value;
import name.ignat.minerva.model.AuditLog.MessageFlag.Reason;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.address.AddressMatchers;
import name.ignat.minerva.model.address.Domain;
import name.ignat.minerva.model.source.AddressMatcherSource;
import name.ignat.minerva.model.source.AddressSource;
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

    public void addInitial(@Nonnull Collection<Address> addresses, @Nonnull AddressSource source, boolean filter)
    {
        addresses.stream().forEach(address -> add(address, source, filter));
    }

    public boolean add(@Nonnull Address address, @Nonnull AddressSource source, boolean filter)
    {
        return add(address, source, null, filter);
    }

    public boolean add(@Nonnull Address address, @Nonnull AddressSource source, @Nullable Rule matchedRule)
    {
        return add(address, source, matchedRule, true);
    }

    /*
     * Filter checks are done in order of increasing severity (DUPLICATE, EXCLUDED, FLAGGED), to minimize how many
     * high-severity filter actions must be manually analyzed in the output file.
     */
    private boolean add(@Nonnull Address address, @Nonnull AddressSource source, @Nullable Rule matchedRule,
        boolean filter)
    {
        if (addressesByDomain.containsEntry(address.getDomain(), address))
        {
            auditLog.onAddressDuplicate(address, source, matchedRule);

            return false;
        }
        else if (filter && addressFilters.shouldExclude(address))
        {
            Set<AddressMatcherSource> exclusionSources = addressFilters.getExclusionSources(address);

            auditLog.onAddressExcluded(address, source, exclusionSources, matchedRule);

            return false;
        }
        else if (filter && addressFilters.shouldFlag(address))
        {
            Set<AddressMatcherSource> flagSources = addressFilters.getFlagSources(address);

            auditLog.onAddressFlagged(address, source, flagSources, matchedRule);

            return false;
        }
        else
        {
            boolean changed = addressesByDomain.put(address.getDomain(), address);

            assertThat(changed, is(true));

            auditLog.onAddressAdded(address, source, matchedRule);

            return true;
        }
    }

    public boolean remove(@Nonnull Address address, @Nonnull AddressSource source, @Nullable Rule matchedRule)
    {
        boolean changed = addressesByDomain.remove(address.getDomain(), address);

        if (changed)
        {
            auditLog.onAddressRemoved(address, source, matchedRule);
        }
        else
        {
            auditLog.onAddressRemoveNotApplicable(address, source, matchedRule);
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
        private final AddressMatchers.Builder exclusionMatchersBuilder = AddressMatchers.builder();
        private final AddressMatchers.Builder flagMatchersBuilder = AddressMatchers.builder();

        private final List<InitialAddressesTuple> initialAddressesTuples = new ArrayList<>();

        private Builder() { }

        public AddressMatchers.Builder exclusionMatchersBuilder()
        {
            return exclusionMatchersBuilder;
        }

        public AddressMatchers.Builder flagMatchersBuilder()
        {
            return flagMatchersBuilder;
        }

        public Builder addInitial(List<Address> addresses, AddressSource source, boolean filter)
        {
            initialAddressesTuples.add(new InitialAddressesTuple(addresses, source, filter));
            return this;
        }

        public AddressBook build()
        {
            AddressBook addressBook = new AddressBook(
                new AddressFilters(exclusionMatchersBuilder.build(), flagMatchersBuilder.build()));

            initialAddressesTuples.stream().forEach(
                tuple -> addressBook.addInitial(tuple.getAddresses(), tuple.getSource(), tuple.isFilter()));

            return addressBook;
        }

        @Value
        private class InitialAddressesTuple
        {
            private List<Address> addresses;
            private AddressSource source;
            private boolean filter;
        }
    }
}
