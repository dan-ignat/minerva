package name.ignat.minerva.rule.impl;

import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.NO_BODY_ADDRESSES;
import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.NO_FROM_ADDRESS;
import static name.ignat.minerva.model.source.MainMessageFileSource.MessageField.BODY;
import static name.ignat.minerva.model.source.MainMessageFileSource.MessageField.FROM;

import java.util.Set;

import lombok.EqualsAndHashCode;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.source.MainMessageFileSource;
import name.ignat.minerva.rule.Rule;

@EqualsAndHashCode
abstract class RuleBase implements Rule
{
    private final String messageFilePath;

    public RuleBase(String messageFilePath)
    {
        this.messageFilePath = messageFilePath;
    }

    protected void addFromAddress(Message message, AddressBook addressBook)
    {
        Address from = message.getFrom();

        if (from == null)
        {
            addressBook.flagMessage(message, this, NO_FROM_ADDRESS);
        }
        else
        {
            MainMessageFileSource source = new MainMessageFileSource(messageFilePath, message, FROM);

            addressBook.add(from, source, this);
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
            MainMessageFileSource source = new MainMessageFileSource(messageFilePath, message, FROM);

            addressBook.remove(from, source, this);
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
            MainMessageFileSource source = new MainMessageFileSource(messageFilePath, message, BODY);

            addresses.stream().forEach(address -> addressBook.add(address, source, this));
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
            MainMessageFileSource source = new MainMessageFileSource(messageFilePath, message, BODY);

            addresses.stream().forEach(address -> addressBook.remove(address, source, this));
        }
    }
}
