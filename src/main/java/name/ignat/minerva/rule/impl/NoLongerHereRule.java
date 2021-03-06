package name.ignat.minerva.rule.impl;

import static name.ignat.commons.lang.Strings.containsAnyIgnoreCase;
import static name.ignat.minerva.rule.Rule.Signal.STOP;

import lombok.EqualsAndHashCode;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;

/**
 * @author Dan Ignat
 */
@EqualsAndHashCode(callSuper = true)
public class NoLongerHereRule extends AutoReplyRule
{
    private static final String[] BODY_KEYPHRASES = { "no longer", "any longer" };

    public NoLongerHereRule(String messageFilePath)
    {
        super(messageFilePath);
    }

    @Override
    public boolean matches(Message message, AddressBook addressBook)
    {
        return containsAnyIgnoreCase(message.getBody(), BODY_KEYPHRASES);
    }

    @Override
    public Signal run(Message message, AddressBook addressBook)
    {
        removeFromAddress(message, addressBook);

        // It's unusual for a NoLongerHere message to have no addresses in its body, so that should cause flagging
        addBodyAddresses(message, addressBook, true);

        return STOP;
    }
}
