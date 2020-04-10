package name.ignat.minerva.io.read;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import name.ignat.commons.exception.UnexpectedCaseException;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.address.AddressPattern;
import name.ignat.minerva.model.address.Domain;

public final class ReadMappers
{
    /*
     * Can't use actual type variables for these, since they have to vary for each call to register() and
     * registerFactory().
     */
    private static Map<Class<?>, ReadMapper<?>> registry = new LinkedHashMap<>();

    /*
     * Sometimes a mapper needs to have state, e.g. MessageMapper.messageIndex.  So we use a registry for stateless
     * mappers, and a factoryRegistry for stateful mappers.
     */
    private static Map<Class<?>, Supplier<ReadMapper<?>>> factoryRegistry = new LinkedHashMap<>();

    static
    {
        register(Address.class,        ReadMappers::toAddress);
        register(Domain.class,         ReadMappers::toDomain);
        register(AddressPattern.class, ReadMappers::toAddressPattern);

        registerFactory(Message.class, MessageMapper::new);
    }

    // Prevents registering the wrong ReadMapper for a given Class
    private static <T> void register(Class<T> clazz, ReadMapper<T> mapper)
    {
        registry.put(clazz, mapper);
    }

    /*
     * Meant to prevent registering the wrong ReadMapper factory for a given Class.  Unfortunately, Java doesn't let me
     * use T for the mapperFactory, due to limitations on nested generics:
     * 
     * "The method put(Class<?>, Supplier<ReadMapper<?>>) in the type Map<Class<?>,Supplier<ReadMapper<?>>> is not
     * applicable for the arguments (Class<T>, Supplier<ReadMapper<T>>)"
     * 
     * So this method doesn't actually prevent what it's supposed to.
     */
    private static <T> void registerFactory(Class<T> clazz, Supplier<ReadMapper<?>> mapperFactory)
    {
        factoryRegistry.put(clazz, mapperFactory);
    }

    @SuppressWarnings("unchecked")
    public static <T> ReadMapper<T> forClass(Class<T> clazz)
    {
        ReadMapper<T> objectMapper = (ReadMapper<T>) registry.get(clazz);

        if (objectMapper == null)
        {
            /*
             * Can't use T here, due to same nested generics limitations as mentioned above:
             * 
             * "Cannot cast from Supplier<ReadMapper<?>> to Supplier<ReadMapper<T>>"
             */
            Supplier<ReadMapper<?>> objectMapperFactory = factoryRegistry.get(clazz);

            if (objectMapperFactory == null)
            {
                throw new UnexpectedCaseException(clazz);
            }

            objectMapper = (ReadMapper<T>) objectMapperFactory.get();
        }

        return objectMapper;
    }

    public static Address toAddress(String[] args)
    {
        assertThat(args, arrayWithSize(1));

        return new Address(args[0]);
    }

    public static Domain toDomain(String[] args)
    {
        assertThat(args, arrayWithSize(1));

        return new Domain(args[0]);
    }

    public static AddressPattern toAddressPattern(String[] args)
    {
        assertThat(args, arrayWithSize(1));

        return new AddressPattern(args[0]);
    }

    public static class MessageMapper implements ReadMapper<Message>
    {
        private int messageIndex = 2;

        @Override
        public Message apply(String[] args)
        {
            assertThat(args, arrayWithSize(3));

            return new Message(messageIndex++, args[0], args[1], args[2]);
        }
    }

    private ReadMappers() { }
}
