package name.ignat.minerva.model.address;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.groupingBy;
import static name.ignat.minerva.model.address.AddressMatcher.Type.ADDRESS;
import static name.ignat.minerva.model.address.AddressMatcher.Type.DOMAIN;
import static name.ignat.minerva.model.address.AddressMatcher.Type.PATTERN;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import name.ignat.minerva.model.source.ContactFileSource;

/**
 * @author Dan Ignat
 */
public class AddressMatchersUtils
{
    public static AddressMatchers fromStrings(List<String> strings)
    {
        Map<AddressMatcher.Type, ImmutableList<String>> groupMap =
            strings.stream().collect(groupingBy(AddressMatcher.Type::fromExpression, toImmutableList()));

        List<String> addressStrings = groupMap.get(ADDRESS);
        List<String> domainStrings = groupMap.get(DOMAIN);
        List<String> patternStrings = groupMap.get(PATTERN);

        List<Address> addresses = addressStrings == null ? ImmutableList.of() : Address.fromStrings(addressStrings);
        List<Domain> domains = domainStrings == null   ? ImmutableList.of() : Domain.fromStrings(domainStrings);
        List<AddressPattern> patterns =
            patternStrings == null  ? ImmutableList.of() : AddressPattern.fromStrings(patternStrings);

        return AddressMatchers.builder()
            .addAddresses(addresses, new ContactFileSource(null, null))
            .addDomains(domains, new ContactFileSource(null, null))
            .addPatterns(patterns, new ContactFileSource(null, null))
            .build();
    }
}
