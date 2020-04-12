package name.ignat.minerva.model.source;

import javax.annotation.concurrent.Immutable;

import lombok.Value;
import lombok.experimental.NonFinal;

@Immutable
@Value @NonFinal
public class DummyAddressMatcherSource implements AddressMatcherSource
{
    @Override
    public String describe()
    {
        return null;
    }
}
