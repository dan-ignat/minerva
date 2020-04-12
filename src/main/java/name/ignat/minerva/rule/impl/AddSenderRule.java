package name.ignat.minerva.rule.impl;

import static name.ignat.minerva.rule.Rule.Signal.PROCEED;

import lombok.EqualsAndHashCode;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;

@EqualsAndHashCode(callSuper = true)
public class AddSenderRule extends NormalReplyRule
{
    public AddSenderRule(String messageFilePath)
    {
        super(messageFilePath);
    }

    @Override
    public boolean matches(Message message, AddressBook addressBook)
    {
        return true;
    }

    @Override
    public Signal run(Message message, AddressBook addressBook)
    {
        addFromAddress(message, addressBook);

        // This is for messages via e.g. LinkedIn, which hide the From address, but sometimes people include their
        // address in the body
        addBodyAddresses(message, addressBook, false);

        return PROCEED;
    }
}
