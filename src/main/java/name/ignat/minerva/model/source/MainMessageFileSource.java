package name.ignat.minerva.model.source;

import javax.annotation.concurrent.Immutable;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import name.ignat.minerva.model.Message;

/**
 * @author Dan Ignat
 */
@Immutable
@Value @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
public class MainMessageFileSource extends FileSource implements AddressSource
{
    private Message message;
    private MessageField field;

    public MainMessageFileSource(String path, Message message, MessageField field)
    {
        super(path);
        this.message = message;
        this.field = field;
    }

    @Override
    public Integer getMessageIndex()
    {
        return message.getIndex();
    }

    @Override
    public String describe()
    {
        return field.toString();
    }

    public static enum MessageField { FROM, BODY }
}
