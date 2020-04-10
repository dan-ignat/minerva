package name.ignat.minerva.model;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.groupingBy;
import static name.ignat.minerva.model.address.AddressMatcher.Type.ADDRESS;
import static name.ignat.minerva.model.address.AddressMatcher.Type.DOMAIN;
import static name.ignat.minerva.model.address.AddressMatcher.Type.PATTERN;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.address.AddressMatcher;
import name.ignat.minerva.model.address.AddressPattern;
import name.ignat.minerva.model.address.Domain;

@Immutable
public class AddressMatchers
{
    public static AddressMatchers fromStrings(List<String> strings)
    {
        Map<AddressMatcher.Type, ImmutableList<String>> groupMap =
            strings.stream().collect(groupingBy(AddressMatcher.Type::fromExpression, toImmutableList()));

        List<String> addresses = groupMap.get(ADDRESS);
        List<String> domains = groupMap.get(DOMAIN);
        List<String> patterns = groupMap.get(PATTERN);

        return new AddressMatchers(
            addresses == null ? ImmutableList.of() : Address.fromStrings(addresses),
            domains == null   ? ImmutableList.of() : Domain.fromStrings(domains),
            patterns == null  ? ImmutableList.of() : AddressPattern.fromStrings(patterns)
        );
    }

    private final Set<Address> addresses;
    private final Set<Domain> domains;
    private final Set<AddressPattern> patterns;

    public AddressMatchers()
    {
        addresses = ImmutableSet.of();
        domains = ImmutableSet.of();
        patterns = ImmutableSet.of();
    }

    public AddressMatchers(
        Collection<Address> addresses, Collection<Domain> domains, Collection<AddressPattern> patterns)
    {
        this.addresses = ImmutableSet.copyOf(addresses);
        this.domains = ImmutableSet.copyOf(domains);
        this.patterns = ImmutableSet.copyOf(patterns);
    }

    public boolean match(Address address)
    {
        return
            addresses.contains(address) ||
            domains.stream().anyMatch(domain -> domain.matches(address)) ||
            patterns.stream().anyMatch(pattern -> pattern.matches(address));
    }
}
