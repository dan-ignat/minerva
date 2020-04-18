package name.ignat.minerva.model.address;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.UNICODE_CASE;
import static name.ignat.minerva.util.Strings.isWrapped;
import static org.apache.commons.lang3.StringUtils.unwrap;
import static org.apache.commons.lang3.StringUtils.wrap;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import lombok.EqualsAndHashCode;

/**
 * @author Dan Ignat
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
public class AddressPattern extends AddressMatcher
{
    private static final String WRAP_TOKEN = "/";

    public static boolean isValid(String expression)
    {
        try
        {
            new AddressPattern(expression);

            return true;
        }
        catch (ValidationException | PatternSyntaxException e)
        {
            return false;
        }
    }

    public static List<AddressPattern> fromStrings(List<String> strings)
    {
        return strings.stream().map(AddressPattern::new).collect(toImmutableList());
    }

    private final Pattern pattern;

    public AddressPattern(@Nonnull String patternString) throws ValidationException
    {
        patternString = patternString.trim();

        if (!isWrapped(patternString, WRAP_TOKEN))
        {
            throw new ValidationException(patternString);
        }

        patternString = unwrap(patternString, WRAP_TOKEN);

        /*
         * Does case-insensitive matching, since converting the patterns to lower-case is not an option, as that would
         * have unintended consequences on character classes, boundaries, and other regex constructs that use upper-case
         * letters
         */
        pattern = Pattern.compile(patternString, CASE_INSENSITIVE | UNICODE_CASE);
    }

    @Override
    public boolean matches(Address address)
    {
        return pattern.matcher(address.toCanonical()).find();
    }

    @Override
    public String toCanonical()
    {
        return wrap(pattern.pattern(), WRAP_TOKEN);
    }
}
