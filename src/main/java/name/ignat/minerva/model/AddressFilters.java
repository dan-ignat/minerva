package name.ignat.minerva.model;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.address.AddressMatchers;
import name.ignat.minerva.model.source.AddressMatcherSource;

/**
 * @author Dan Ignat
 */
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

    public AddressFilters(List<AddressFilters> addressFiltersList)
    {
        this.exclusionMatchers = new AddressMatchers(
            addressFiltersList.stream().map(addressFilters -> addressFilters.exclusionMatchers).collect(toList()));

        this.flagMatchers = new AddressMatchers(
            addressFiltersList.stream().map(addressFilters -> addressFilters.flagMatchers).collect(toList()));
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
