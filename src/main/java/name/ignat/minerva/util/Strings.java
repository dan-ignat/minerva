package name.ignat.minerva.util;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Arrays.stream;
import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.replaceEach;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/**
 * @author Dan Ignat
 */
public final class Strings
{
    public static boolean containsIgnoreCase(String string, String searchString)
    {
        if (string == null || searchString == null)
        {
            return false;
        }

        return string.toLowerCase().contains(searchString.toLowerCase());
    }

    public static boolean containsAnyIgnoreCase(String string, String... searchStrings)
    {
        return stream(searchStrings).anyMatch(searchString -> containsIgnoreCase(string, searchString));
    }

    public static List<String> findAllMatches(String patternString, String input)
    {
        Pattern pattern = Pattern.compile(patternString);

        return findAllMatches(pattern, input);
    }

    public static List<String> findAllMatches(Pattern pattern, String input)
    {
        return pattern.matcher(input).results().map(MatchResult::group).collect(toImmutableList());
    }

    public static boolean isWrapped(String string, String wrapToken)
    {
        return startsWith(string, wrapToken) && endsWith(string, wrapToken);
    }

    public static String replaceVariables(String text, Map<String, String> variableMap)
    {
        String[] searchList = variableMap.keySet().stream().map(s -> '{' + s + '}').toArray(String[]::new);

        String[] replacementList = variableMap.values().toArray(new String[0]);

        return replaceEach(text, searchList, replacementList);
    }

    private Strings() { }
}
