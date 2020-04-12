package name.ignat.minerva.model;

import java.util.Set;

import javax.annotation.concurrent.Immutable;

import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.address.AddressMatchers;
import name.ignat.minerva.model.source.AddressMatcherSource;

@Immutable
public class AddressFilters
{
    private final AddressMatchers exclusionMatchers;
    private final AddressMatchers flagMatchers;

    public AddressFilters()
    {
        this(new AddressMatchers(), new AddressMatchers());
    }

    public AddressFilters(AddressMatchers exclusionMatchers, AddressMatchers flagMatchers)
    {
        this.exclusionMatchers = exclusionMatchers;
        this.flagMatchers = flagMatchers;
    }

    public boolean shouldExclude(Address address)
    {
        return exclusionMatchers.match(address);
    }

    public Set<AddressMatcherSource> getExclusionSources(Address address)
    {
        return exclusionMatchers.getMatchingSources(address);
    }

    public boolean shouldFlag(Address address)
    {
        return flagMatchers.match(address);
    }

    public Set<AddressMatcherSource> getFlagSources(Address address)
    {
        return flagMatchers.getMatchingSources(address);
    }
}
