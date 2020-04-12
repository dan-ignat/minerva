package name.ignat.minerva.rule.impl;

import static name.ignat.minerva.rule.Rule.Signal.STOP;
import static name.ignat.minerva.util.Strings.containsAnyIgnoreCase;

import lombok.EqualsAndHashCode;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;

@EqualsAndHashCode(callSuper = true)
public class NoLongerHereRule extends AutoReplyRule
{
    private static final String[] keyphrases = { "no longer", "any longer" };

    public NoLongerHereRule(String messageFilePath)
    {
        super(messageFilePath);
    }

    @Override
    public boolean matches(Message message, AddressBook addressBook)
    {
        return containsAnyIgnoreCase(message.getBody(), keyphrases);
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
