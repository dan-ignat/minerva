package name.ignat.minerva.io.write;

import static name.ignat.minerva.util.Describable.describe;

import java.util.LinkedHashMap;
import java.util.Map;

import name.ignat.commons.exception.UnexpectedCaseException;
import name.ignat.minerva.model.AuditLog.AddressEntry;
import name.ignat.minerva.model.AuditLog.MessageFlag;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.address.Domain;
import name.ignat.minerva.util.Array;

/**
 * The {@code from*()} methods return empty string instead of {@code null}, because CSVs have no way to distinguish
 * between the two, and e.g. unit tests fail when they read back a CSV and compare to {@code null}.
 * 
 * @author Dan Ignat
 */
public final class WriteMappers
{
    private static Map<Class<?>, WriteMapper<?>> registry = new LinkedHashMap<>();

    static
    {
        register(Address.class,      WriteMappers::fromAddress);
        register(Domain.class,       WriteMappers::fromDomain);
        register(Message.class,      WriteMappers::fromMessage);
        register(MessageFlag.class,  WriteMappers::fromMessageFlag);
        register(AddressEntry.class, WriteMappers::fromAddressEntry);
    }

    // Prevents registering the wrong WriteMapper for a given Class
    private static <T> void register(Class<T> clazz, WriteMapper<T> mapper)
    {
        registry.put(clazz, mapper);
    }

    @SuppressWarnings("unchecked")
    public static <T> WriteMapper<T> forClass(Class<T> clazz)
    {
        WriteMapper<T> objectMapper = (WriteMapper<T>) registry.get(clazz);

        if (objectMapper == null)
        {
            throw new UnexpectedCaseException(clazz);
        }

        return objectMapper;
    }

    public static String[] fromAddress(Address address)
    {
        return Array.of(address.toCanonical());
    }

    public static String[] fromDomain(Domain domain)
    {
        return Array.of(domain.toCanonical());
    }

    public static String[] fromMessage(Message message)
    {
        return Array.of(
            message.getIndex().toString(),
            message.getFrom() == null ? "" : message.getFrom().toCanonical(),
            message.getSubject(),
            message.getBody() == null ? "" : message.getBody());
    }

    public static String[] fromMessageFlag(MessageFlag flag)
    {
        return Array.of(
            flag.getMessage().getIndex().toString(),
            flag.getMatchedRule() == null ? "" : flag.getMatchedRule().getClass().getSimpleName(),
            flag.getReason().toString());
    }

    public static String[] fromAddressEntry(AddressEntry entry)
    {
        Integer messageIndex = entry.getAddressSource().getMessageIndex();

        return Array.of(
            messageIndex == null ? "" : messageIndex.toString(),
            entry.getAddress().toCanonical(),
            entry.getAddressSource().describe(),
            entry.getType().toString(),
            entry.getFilterSources() == null ? "" : describe(entry.getFilterSources()),
            entry.getMatchedRule() == null ? "" : entry.getMatchedRule().getClass().getSimpleName());
    }

    private WriteMappers() { }
}
