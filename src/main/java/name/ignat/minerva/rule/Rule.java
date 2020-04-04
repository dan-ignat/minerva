package name.ignat.minerva.rule;

import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;

public interface Rule
{
    boolean matches(Message message, AddressBook addressBook);

    Signal run(Message message, AddressBook addressBook);

    enum Signal { PROCEED, STOP }
}
