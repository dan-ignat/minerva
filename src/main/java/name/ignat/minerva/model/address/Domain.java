package name.ignat.minerva.model.address;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Math.max;
import static lombok.AccessLevel.NONE;
import static org.apache.commons.lang3.ArrayUtils.reverse;
import static org.apache.commons.lang3.ArrayUtils.swap;
import static org.apache.commons.lang3.StringUtils.split;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

/**
 * The domain portion of an e-mail address.
 * <p>
 * For sorting purposes, we swap the TLD (Top-Level Domain) and 2LD (2nd-Level Domain) components, so that we sort first
 * by 2LD, then TLD.  (Most domain name owners first settle on a given 2LD, and then pick the best TLD available for
 * that 2LD, so it seems better to group/sort by 2LD first.)
 * 
 * @author Dan Ignat <dan@ignat.name>
 */
@Immutable
@Value
@EqualsAndHashCode(callSuper = false)
public class Domain extends AddressMatcher implements Comparable<Domain>
{
    /*
     * These are the only chars that occur in vast majority of real-world domains.  Includes A-Z to match addresses with
     * upper-case letters.
     */
    protected static final String ATOM = "[A-Za-z0-9_-]+";
    static final String PATTERN_STRING = ATOM + "(?:\\." + ATOM + ")+";
    private static final Pattern PATTERN = Pattern.compile(PATTERN_STRING);

    public static boolean isValid(String domain)
    {
        return Addressables.isValid(domain, PATTERN);
    }

    public static List<Domain> fromStrings(List<String> strings)
    {
        return strings.stream().map(Domain::new).collect(toImmutableList());
    }

    private final String domain;

    @Getter(NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final String[] components;

    @Getter(NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final String[] sortingComponents;

    public Domain(String domain) throws ValidationException
    {
        this.domain = Addressables.normalize(domain, PATTERN);

        components = split(this.domain, '.');

        sortingComponents = ArrayUtils.clone(components);

        reverse(sortingComponents);

        // Swap TLD and 2LD so that we sort by 2LD first, then TLD
        swap(sortingComponents, 0, 1);
    }

    public boolean isSubdomainOf(Domain other)
    {
        if (components.length == other.components.length)
        {
            return domain.equals(other.domain);
        }
        else if (components.length > other.components.length)
        {
            return domain.endsWith('.' + other.domain);
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean matches(Address address)
    {
        return address.getDomain().isSubdomainOf(this);
    }

    @Override
    public int compareTo(Domain other)
    {
        CompareToBuilder builder = new CompareToBuilder();

        int max = max(sortingComponents.length, other.sortingComponents.length);

        for (int i = 0; i < max; i++)
        {
            String component      = i < sortingComponents.length ? sortingComponents[i] : null;
            String otherComponent = i < other.sortingComponents.length ? other.sortingComponents[i] : null;

            builder.append(component, otherComponent);
        }

        return builder.toComparison();
    }

    @Override
    public String toCanonical()
    {
        return domain;
    }
}
