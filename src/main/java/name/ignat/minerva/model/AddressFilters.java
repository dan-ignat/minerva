package name.ignat.minerva.model;

import java.util.Collection;

import javax.annotation.concurrent.Immutable;

@Immutable
public class AddressFilters
{
    private final AddressMatchers exclusionMatchers;
    private final AddressMatchers flagMatchers;

    public AddressFilters()
    {
        this(new AddressMatchers(), new AddressMatchers());
    }

    public AddressFilters(
        Collection<Address> exclusionAddresses, Collection<Domain> exclusionDomains,
        Collection<AddressPattern> exclusionPatterns,
        Collection<Address> flagAddresses, Collection<Domain> flagDomains, Collection<AddressPattern> flagPatterns)
    {
        this(
            new AddressMatchers(exclusionAddresses, exclusionDomains, exclusionPatterns),
            new AddressMatchers(flagAddresses, flagDomains, flagPatterns));
    }

    public AddressFilters(AddressMatchers exclusionMatchers, AddressMatchers flagMatchers)
    {
        this.exclusionMatchers = exclusionMatchers;
        this.flagMatchers = flagMatchers;
    }

    public boolean shouldExclude(Address address)
    {
        return exclusionMatchers.match(address) || shouldFlag(address);
    }

    public boolean shouldFlag(Address address)
    {
        return flagMatchers.match(address);
    }
}
