package name.ignat.minerva.model.source;

import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.address.AddressMatcher;
import name.ignat.minerva.util.Describable;

/**
 * The source of an {@link Address}, when not used as an {@link AddressMatcher}.
 * <p>
 * For semantics and API clarity, we need to distinguish when a source is specifically for an initial or output {@code
 * Address}, and not for an {@code Address} being used as an {@code AddressMatcher}.
 * 
 * @author Dan Ignat
 */
public interface AddressSource extends Describable
{
    Integer getMessageIndex();
}
