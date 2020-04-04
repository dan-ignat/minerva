package name.ignat.minerva.model;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.groupingBy;
import static name.ignat.minerva.model.AddressFilters.AddressMatchers.AddressMatcherType.ADDRESS;
import static name.ignat.minerva.model.AddressFilters.AddressMatchers.AddressMatcherType.DOMAIN;
import static name.ignat.minerva.model.AddressFilters.AddressMatchers.AddressMatcherType.PATTERN;
import static name.ignat.minerva.util.Patterns.DELIMITER;
import static name.ignat.minerva.util.Strings.isWrapped;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import name.ignat.commons.exception.UnexpectedCaseException;
import name.ignat.minerva.util.Patterns;

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

    public AddressFilters(
        Collection<Address> exclusionAddresses, Collection<Domain> exclusionDomains, Collection<Pattern> exclusionPatterns,
        Collection<Address> flagAddresses, Collection<Domain> flagDomains, Collection<Pattern> flagPatterns)
    {
        this(
            new AddressMatchers(exclusionAddresses, exclusionDomains, exclusionPatterns),
            new AddressMatchers(flagAddresses, flagDomains, flagPatterns));
    }

    public boolean shouldExclude(Address address)
    {
        return exclusionMatchers.match(address) || shouldFlag(address);
    }

    public boolean shouldFlag(Address address)
    {
        return flagMatchers.match(address);
    }

    @Immutable
    public static class AddressMatchers
    {
        public static AddressMatchers fromStrings(List<String> strings)
        {
            Map<AddressMatcherType, ImmutableList<String>> groupMap =
                strings.stream().collect(groupingBy(AddressMatcherType::fromExpression, toImmutableList()));

            List<String> addresses = groupMap.get(ADDRESS);
            List<String> domains = groupMap.get(DOMAIN);
            List<String> patterns = groupMap.get(PATTERN);

            return new AddressMatchers(
                addresses == null ? ImmutableList.of() : Address.fromStrings(addresses),
                domains == null   ? ImmutableList.of() : Domain.fromStrings(domains),
                patterns == null  ? ImmutableList.of() : Patterns.fromDelimitedStrings(patterns)
            );
        }

        private final Set<Address> addresses;
        private final Set<Domain> domains;
        private final Set<Pattern> patterns;

        public AddressMatchers()
        {
            addresses = ImmutableSet.of();
            domains = ImmutableSet.of();
            patterns = ImmutableSet.of();
        }

        public AddressMatchers(Collection<Address> addresses, Collection<Domain> domains, Collection<Pattern> patterns)
        {
            this.addresses = ImmutableSet.copyOf(addresses);
            this.domains = ImmutableSet.copyOf(domains);
            this.patterns = ImmutableSet.copyOf(patterns);
        }

        public boolean match(Address address)
        {
            return
                addresses.contains(address) ||
                domains.stream().anyMatch(domain -> address.getDomain().isSubdomainOf(domain)) ||
                patterns.stream().anyMatch(pattern -> pattern.matcher(address.toCanonical()).find());
        }

        public enum AddressMatcherType
        {
            ADDRESS, DOMAIN, PATTERN;

            public static AddressMatcherType fromExpression(String expression)
            {
                if (Address.isValid(expression))
                    return ADDRESS;
                else if (Domain.isValid(expression))
                    return DOMAIN;
                else if (isWrapped(expression, DELIMITER))
                    return PATTERN;
                else
                    throw new UnexpectedCaseException(expression);
            };
        };
    }
}
