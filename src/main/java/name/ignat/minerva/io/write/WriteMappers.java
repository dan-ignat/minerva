package name.ignat.minerva.io.write;

import static name.ignat.minerva.util.Describable.describe;
import static name.ignat.minerva.util.Objects.ifNotNull;

import java.util.LinkedHashMap;
import java.util.Map;

import name.ignat.commons.exception.UnexpectedCaseException;
import name.ignat.minerva.model.AuditLog.AddressEntry;
import name.ignat.minerva.model.AuditLog.MessageFlag;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.address.Domain;
import name.ignat.minerva.util.Array;

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
            ifNotNull(message.getFrom(), f -> f.toCanonical()),
            message.getSubject(),
            message.getBody());
    }

    public static String[] fromMessageFlag(MessageFlag flag)
    {
        return Array.of(
            flag.getMessage().getIndex().toString(),
            ifNotNull(flag.getMatchedRule(), r -> r.getClass().getSimpleName()),
            flag.getReason().toString());
    }

    public static String[] fromAddressEntry(AddressEntry entry)
    {
        Integer messageIndex = entry.getAddressSource().getMessageIndex();

        return Array.of(
            // This breaks tests, due to assertion expecting null but getting back "" from the read CSV
            //messageIndex == null ? null : messageIndex.toString(),
            messageIndex == null ? "" : messageIndex.toString(),
            entry.getAddress().toCanonical(),
            entry.getAddressSource().describe(),
            entry.getType().toString(),
            ifNotNull(entry.getFilterSources(), fs -> describe(fs)),
            ifNotNull(entry.getMatchedRule(), r -> r.getClass().getSimpleName()));
    }

    private WriteMappers() { }
}
