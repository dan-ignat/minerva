package name.ignat.minerva.util;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.UNICODE_CASE;
import static name.ignat.minerva.util.Strings.isWrapped;
import static org.apache.commons.lang3.StringUtils.unwrap;

import java.util.List;
import java.util.regex.Pattern;

import name.ignat.commons.exception.UnexpectedCaseException;

public final class Patterns
{
    public static final String DELIMITER = "/";

    /**
     * Does case-insensitive matching, since converting the patterns to lower-case is not an option, as that would
     * have unintended consequences on character classes, boundaries, and other regex constructs that use upper-case
     * letters.
     */
    public static List<Pattern> fromDelimitedStrings(List<String> strings)
    {
        return fromDelimitedStrings(strings, false);
    }

    public static List<Pattern> fromDelimitedStrings(List<String> strings, boolean caseSensitive)
    {
        return strings.stream().map(string -> fromDelimitedString(string, caseSensitive)).collect(toImmutableList());
    }

    public static Pattern fromDelimitedString(String string)
    {
        return fromDelimitedString(string, false);
    }

    public static Pattern fromDelimitedString(String string, boolean caseSensitive)
    {
        if (!isWrapped(string, DELIMITER))
        {
            throw new UnexpectedCaseException(string);
        }

        string = unwrap(string, DELIMITER);

        return caseSensitive ? Pattern.compile(string) : Pattern.compile(string, CASE_INSENSITIVE | UNICODE_CASE);
    }

    /*
    public static List<Pattern> fromStrings(List<String> strings)
    {
        return fromStrings(strings, false);
    }

    public static List<Pattern> fromStrings(List<String> strings, boolean caseSensitive)
    {
        return strings.stream()
            .map(caseSensitive ? Pattern::compile : s -> Pattern.compile(s, CASE_INSENSITIVE | UNICODE_CASE))
            .collect(toImmutableList());
    }
    */

    private Patterns() { }
}
