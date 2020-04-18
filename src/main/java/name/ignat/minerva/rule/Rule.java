package name.ignat.minerva.rule;

import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.model.address.Address;

/**
 * A rule used to match {@link Message}s and add (remove) their contained {@link Address}es to (from) the {@link
 * AddressBook}.
 * 
 * @author Dan Ignat
 */
public interface Rule
{
    boolean matches(Message message, AddressBook addressBook);

    Signal run(Message message, AddressBook addressBook);

    enum Signal { PROCEED, STOP }
}
