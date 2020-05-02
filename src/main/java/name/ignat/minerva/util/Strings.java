package name.ignat.minerva.util;

import static org.apache.commons.lang3.StringUtils.endsWith;
import static org.apache.commons.lang3.StringUtils.replaceEach;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.util.Map;

/**
 * @author Dan Ignat
 */
public final class Strings
{
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
