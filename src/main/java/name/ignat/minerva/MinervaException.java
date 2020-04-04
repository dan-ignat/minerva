package name.ignat.minerva;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("serial")
public class MinervaException extends RuntimeException
{
    public MinervaException(String message)
    {
        super(message);
    }

    @EqualsAndHashCode.Include
    @Override
    public String getMessage() { return super.getMessage(); }
}
