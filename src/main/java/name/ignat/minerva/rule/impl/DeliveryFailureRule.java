package name.ignat.minerva.rule.impl;

import static name.ignat.minerva.rule.Rule.Signal.STOP;
import static name.ignat.minerva.util.Strings.containsAnyIgnoreCase;

import lombok.EqualsAndHashCode;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;

@EqualsAndHashCode(callSuper = true)
public class DeliveryFailureRule extends AutoReplyRule
{
    private static final String[] keyphrases = { "undeliverable", "failure" };

    public DeliveryFailureRule(String messageFilePath)
    {
        super(messageFilePath);
    }

    @Override
    public boolean matches(Message message, AddressBook addressBook)
    {
        return containsAnyIgnoreCase(message.getSubject(), keyphrases);
    }

    @Override
    public Signal run(Message message, AddressBook addressBook)
    {
        // It's unusual for a DeliveryFailure message to have no addresses in its body, so that should cause flagging
        removeBodyAddresses(message, addressBook, true);

        return STOP;
    }
}
