package name.ignat.minerva.model.source;

import javax.annotation.concurrent.Immutable;

import lombok.EqualsAndHashCode;
import lombok.Value;

@Immutable
@Value @EqualsAndHashCode(callSuper = true)
public class DummyAddressSource extends DummyAddressMatcherSource implements AddressSource
{
    @Override
    public Integer getMessageIndex()
    {
        return null;
    }
}
