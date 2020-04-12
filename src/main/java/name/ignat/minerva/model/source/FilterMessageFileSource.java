package name.ignat.minerva.model.source;

import javax.annotation.concurrent.Immutable;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Immutable
@Value @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
public class FilterMessageFileSource extends FileSource implements AddressMatcherSource
{
    public FilterMessageFileSource(String path)
    {
        super(path);
    }

    @Override
    public String describe()
    {
        return getPath();
    }
}
