package name.ignat.minerva.model;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static name.ignat.minerva.util.Strings.findAllMatches;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.EqualsAndHashCode;
import lombok.Value;

/**
 * An e-mail address.
 * 
 * @author Dan Ignat
 */
@Immutable
@Value
@EqualsAndHashCode(callSuper = false)
public class Address extends Addressable<Address>
{
    /*
     * These are the only chars that occur in vast majority of real-world addresses.  Includes A-Z to match addresses
     * with upper-case letters.
     */
    protected static final String ATOM = "[A-Za-z0-9._-]+";
    private static final String PATTERN_STRING = ATOM + "@" + Domain.PATTERN_STRING;
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    public static boolean isValid(String address)
    {
        return Addressable.isValid(address, PATTERN);
    }

    public static List<Address> fromStrings(List<String> strings)
    {
        return strings.stream().map(Address::new).collect(toImmutableList());
    }

    public static Set<Address> fromStringsAsSet(List<String> strings)
    {
        return strings.stream().map(Address::new).collect(toImmutableSet());
    }

    public static List<Address> fromValidStrings(List<String> strings)
    {
        return strings.stream().filter(Address::isValid).map(Address::new).collect(toImmutableList());
    }

    public static Set<Address> extractAll(String string)
    {
        return findAllMatches(PATTERN, string).stream()
            .filter(TopLevelDomains::isValidAddressable)
            .map(Address::new)
            .collect(toImmutableSet());
    }

    private final String user;
    private final Domain domain;

    public Address(String address) throws ValidationException
    {
        address = normalize(address, PATTERN);

        this.user = substringBefore(address, "@");
        this.domain = new Domain(substringAfter(address, "@"));
    }

    public boolean belongsTo(Domain other)
    {
        return domain.isSubdomainOf(other);
    }

    /**
     * {@code domain} is compared before {@code user}
     */
    @Override
    public int compareTo(Address other)
    {
        return new CompareToBuilder()
            .append(domain, other.domain)
            .append(user, other.user)
            .toComparison();
    }

    @Override
    public String toCanonical()
    {
        return user + "@" + domain.toCanonical();
    }
}
