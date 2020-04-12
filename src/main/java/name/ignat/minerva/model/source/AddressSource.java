package name.ignat.minerva.model.source;

import name.ignat.minerva.util.Describable;

/**
 * For semantics and API clarity, we need to distinguish when a source is specifically for an initial or output {@code
 * Address}, and not an {@code Address} being used as an {@code AddressMatcher}.
 * 
 * @author Dan Ignat
 */
public interface AddressSource extends Describable
{
    Integer getMessageIndex();
}
