package name.ignat.minerva.model.source;

import java.io.File;

import javax.annotation.concurrent.Immutable;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * @author Dan Ignat
 */
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
        return new File(getPath()).getName();
    }
}
