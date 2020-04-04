package name.ignat.minerva.rule.impl;

import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.NO_BODY_ADDRESSES;
import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.NO_FROM_ADDRESS;

import java.util.Set;

import lombok.EqualsAndHashCode;
import name.ignat.minerva.model.Address;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.rule.Rule;

@EqualsAndHashCode
abstract class RuleBase implements Rule
{
    protected void addFromAddress(Message message, AddressBook addressBook)
    {
        Address from = message.getFrom();

        if (from == null)
        {
            addressBook.flagMessage(message, this, NO_FROM_ADDRESS);
        }
        else
        {
            addressBook.add(from, message, this);
        }
    }

    protected void removeFromAddress(Message message, AddressBook addressBook)
    {
        Address from = message.getFrom();

        if (from == null)
        {
            addressBook.flagMessage(message, this, NO_FROM_ADDRESS);
        }
        else
        {
            addressBook.remove(from, message, this);
        }
    }

    protected void addBodyAddresses(Message message, AddressBook addressBook, boolean flagIfNone)
    {
        Set<Address> addresses = message.getBodyAddresses();

        if (addresses.isEmpty())
        {
            if (flagIfNone)
            {
                addressBook.flagMessage(message, this, NO_BODY_ADDRESSES);
            }
        }
        else
        {
            addresses.stream().forEach(a -> addressBook.add(a, message, this));
        }
    }

    protected void removeBodyAddresses(Message message, AddressBook addressBook, boolean flagIfNone)
    {
        Set<Address> addresses = message.getBodyAddresses();

        if (addresses.isEmpty())
        {
            if (flagIfNone)
            {
                addressBook.flagMessage(message, this, NO_BODY_ADDRESSES);
            }
        }
        else
        {
            addresses.stream().forEach(a -> addressBook.remove(a, message, this));
        }
    }
}
