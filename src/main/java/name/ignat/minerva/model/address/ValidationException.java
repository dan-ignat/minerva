package name.ignat.minerva.model.address;

import static java.lang.String.format;

import name.ignat.minerva.MinervaException;

/**
 * @author Dan Ignat
 */
@SuppressWarnings("serial")
public class ValidationException extends MinervaException
{
    public ValidationException(String addressable)
    {
        super(format("Invalid format: %s", addressable));
    }
}
