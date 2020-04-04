package name.ignat.minerva.model;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static name.ignat.commons.utils.IoUtils.getClassPathResource;
import static org.apache.commons.io.IOUtils.readLines;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import name.ignat.commons.exception.UnexpectedException;

public final class TopLevelDomains
{
    private static final Set<String> tlds;

    static
    {
        try
        {
            InputStream tldFileIn = getClassPathResource("tlds-alpha-by-domain.txt");

            tlds = readLines(tldFileIn)
                .stream()
                .filter(line -> !line.strip().startsWith("#") && !line.isBlank())
                .map(line -> line.strip().toLowerCase())
                .collect(toImmutableSet());
        }
        catch (IOException e)
        {
            throw new UnexpectedException(e);
        }
    }

    public static boolean isValidAddressable(String addressable)
    {
        String tld = substringAfterLast(addressable.toLowerCase(), ".");

        return tlds.contains(tld);
    }

    public TopLevelDomains() { }
}
