package name.ignat.minerva.model.source;

import javax.annotation.concurrent.Immutable;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Immutable
@Value @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
public class ContactFileSource extends FileSource implements AddressMatcherSource, AddressSource
{
    private String sheetName;

    public ContactFileSource(String path, String sheetName)
    {
        super(path);
        this.sheetName = sheetName;
    }

    @Override
    public Integer getMessageIndex()
    {
        return null;
    }

    @Override
    public String describe()
    {
        return sheetName;
    }
}
