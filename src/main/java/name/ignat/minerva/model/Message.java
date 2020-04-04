package name.ignat.minerva.model;

import static com.google.common.flogger.FluentLogger.forEnclosingClass;

import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.flogger.FluentLogger;

import lombok.NonNull;
import lombok.Value;

@Immutable
@Value
public class Message
{
    private static final FluentLogger logger = forEnclosingClass();

    private final Integer index;
    private final Address from;
    private final String  fromRaw;
    private final String  subject;
    private final String  body;

    private final Set<Address> bodyAddresses;

    /**
     * @param body may be {@code null} in certain cases, e.g. when Outlook encounters odd characters that cause it to
     * export the body as empty
     */
    public Message(@NonNull Integer index, @NonNull String from, @NonNull String subject, @Nullable String body)
    {
        this.index = index;

        Address fromAddress;
        try
        {
            fromAddress = new Address(from);
        }
        catch (Address.ValidationException e)
        {
            fromAddress = null;
        }
        this.from = fromAddress;
        this.fromRaw = fromAddress == null ? from : null;

        this.subject = subject;
        this.body = body;

        this.bodyAddresses = body == null ? Set.of() : Address.extractAll(body);

        if (body == null)
        {
            logger.atWarning().log("Missing body for Message at index %d {from: \"%s\", subject: \"%s\"}",
                index, from, subject);
        }
    }
}
