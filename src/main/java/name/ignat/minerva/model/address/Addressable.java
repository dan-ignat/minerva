package name.ignat.minerva.model.address;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.regex.Pattern;

/**
 * @see http://www.regular-expressions.info/email.html
 * @see http://tools.ietf.org/html/rfc2822#section-3.4.1
 * 
 * @author Dan Ignat <dan@ignat.name>
 */
public abstract class Addressable<T> extends AddressMatcher implements Comparable<T>
{
    // This is too permissive, and allows some matches that it shouldn't, so I moved it to subclasses
    //protected static final String ATOM = "[A-Za-z0-9~`!#$%^&*_\\-+={}|'?/]+";

    protected static boolean isValid(String addressable, Pattern pattern)
    {
        try
        {
            normalize(addressable, pattern);

            return true;
        }
        catch (ValidationException e)
        {
            return false;
        }
    }

    protected static String normalize(String addressable, Pattern pattern) throws ValidationException
    {
        checkNotNull(addressable);

        addressable = addressable.trim().toLowerCase();

        if (!pattern.matcher(addressable).matches())
        {
            throw new ValidationException(addressable);
        }

        return addressable;
    }
}
