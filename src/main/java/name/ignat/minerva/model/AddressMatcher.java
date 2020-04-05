package name.ignat.minerva.model;

import static name.ignat.minerva.util.LombokUtils.toCustomString;

import name.ignat.commons.exception.UnexpectedCaseException;
import name.ignat.minerva.util.Canonizable;

public abstract class AddressMatcher implements Canonizable
{
    public abstract boolean matches(Address address);

    @Override
    public String toString()
    {
        return toCustomString(this);
    }

    public enum Type
    {
        ADDRESS, DOMAIN, PATTERN;

        public static Type fromExpression(String expression)
        {
            if (Address.isValid(expression))
                return ADDRESS;
            else if (Domain.isValid(expression))
                return DOMAIN;
            else if (AddressPattern.isValid(expression))
                return PATTERN;
            else
                throw new UnexpectedCaseException(expression);
        };
    };
}
