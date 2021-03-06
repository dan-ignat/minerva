package name.ignat.minerva.rule.impl;

import static name.ignat.minerva.rule.Rule.Signal.PROCEED;

import lombok.EqualsAndHashCode;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;

/**
 * @author Dan Ignat
 */
@EqualsAndHashCode(callSuper = true)
public class RemoveSenderRule extends NormalReplyRule
{
    public RemoveSenderRule(String messageFilePath)
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
        removeFromAddress(message, addressBook);

        // This is for messages via e.g. LinkedIn, which hide the From address, but sometimes people include their
        // address in the body
        removeBodyAddresses(message, addressBook, false);

        return PROCEED;
    }
}
