package name.ignat.minerva.model.address;

import static name.ignat.minerva.util.Multimaps.combiningSetMultimapsImmutably;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;

import name.ignat.minerva.model.source.AddressMatcherSource;

@Immutable
public class AddressMatchers
{
    public static Builder builder()
    {
        return new Builder();
    }

    private final SetMultimap<Address, AddressMatcherSource> addresses;
    private final SetMultimap<Domain, AddressMatcherSource> domains;
    private final SetMultimap<AddressPattern, AddressMatcherSource> patterns;

    public AddressMatchers()
    {
        addresses = ImmutableSetMultimap.of();
        domains = ImmutableSetMultimap.of();
        patterns = ImmutableSetMultimap.of();
    }

    public AddressMatchers(
        SetMultimap<Address, AddressMatcherSource> addresses,
        SetMultimap<Domain, AddressMatcherSource> domains,
        SetMultimap<AddressPattern, AddressMatcherSource> patterns)
    {
        this.addresses = ImmutableSetMultimap.copyOf(addresses);
        this.domains = ImmutableSetMultimap.copyOf(domains);
        this.patterns = ImmutableSetMultimap.copyOf(patterns);
    }

    public AddressMatchers(List<AddressMatchers> addressMatchersList)
    {
        this.addresses = addressMatchersList.stream().map(addressMatchers -> addressMatchers.addresses)
            .collect(combiningSetMultimapsImmutably());

        this.domains = addressMatchersList.stream().map(addressMatchers -> addressMatchers.domains)
            .collect(combiningSetMultimapsImmutably());

        this.patterns = addressMatchersList.stream().map(addressMatchers -> addressMatchers.patterns)
            .collect(combiningSetMultimapsImmutably());
    }

    // TODO: Consider removing this method, and relying entirely on getMatchingSources(), so as not to do two passes
    // through the data structures.
    public boolean match(Address address)
    {
        return
            addresses.containsKey(address) ||
            domains.keySet().stream().anyMatch(domain -> domain.matches(address)) ||
            patterns.keySet().stream().anyMatch(pattern -> pattern.matches(address));
    }

    public Set<AddressMatcherSource> getMatchingSources(Address address)
    {
        ImmutableSet.Builder<AddressMatcherSource> builder = ImmutableSet.builder();

        if (addresses.containsKey(address))
        {
            builder.addAll(addresses.get(address));
        }

        domains.keySet().stream()
            .filter(domain -> domain.matches(address))
            .forEach(domain -> builder.addAll(domains.get(domain)));

        patterns.keySet().stream()
            .filter(pattern -> pattern.matches(address))
            .forEach(pattern -> builder.addAll(patterns.get(pattern)));

        return builder.build();
    }

    public static final class Builder
    {
        private final SetMultimap<Address, AddressMatcherSource> addresses       = LinkedHashMultimap.create(5_000, 10);
        private final SetMultimap<Domain, AddressMatcherSource> domains          = LinkedHashMultimap.create(500,   10);
        private final SetMultimap<AddressPattern, AddressMatcherSource> patterns = LinkedHashMultimap.create(500,   10);

        public Builder addAddress(Address address, AddressMatcherSource source)
        {
            this.addresses.put(address, source);
            return this;
        }

        public Builder addAddresses(Collection<Address> addresses, AddressMatcherSource source)
        {
            addresses.stream().forEach(address -> this.addresses.put(address, source));
            return this;
        }

        public Builder addDomains(Collection<Domain> domains, AddressMatcherSource source)
        {
            domains.stream().forEach(domain -> this.domains.put(domain, source));
            return this;
        }

        public Builder addPatterns(Collection<AddressPattern> patterns, AddressMatcherSource source)
        {
            patterns.stream().forEach(pattern -> this.patterns.put(pattern, source));
            return this;
        }

        public AddressMatchers build()
        {
            return new AddressMatchers(addresses, domains, patterns);
        }

        private Builder() { }
    }
}
