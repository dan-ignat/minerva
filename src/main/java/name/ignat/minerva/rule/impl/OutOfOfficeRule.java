package name.ignat.minerva.rule.impl;

import static name.ignat.minerva.rule.Rule.Signal.STOP;
import static name.ignat.minerva.util.Strings.containsAnyIgnoreCase;

import lombok.EqualsAndHashCode;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;

@EqualsAndHashCode(callSuper = true)
public class OutOfOfficeRule extends AutoReplyRule
{
    private static final String[] subject_keyphrases =
        { "automatic reply", "auto-reply", "autoreply", "auto reply", "auto" };

    private static final String[] subject_body_keyphrases =
        { "out of office", "out of the office", "ooo", "ooto", "vacation" };

    public OutOfOfficeRule(String messageFilePath)
    {
        super(messageFilePath);
    }

    @Override
    public boolean matches(Message message, AddressBook addressBook)
    {
        return
            containsAnyIgnoreCase(message.getSubject(), subject_keyphrases) ||
            containsAnyIgnoreCase(message.getSubject(), subject_body_keyphrases) ||
            containsAnyIgnoreCase(message.getBody(),    subject_body_keyphrases);
    }

    @Override
    public Signal run(Message message, AddressBook addressBook)
    {
        // OutOfOffice messages often have no updates in their body, so that shouldn't cause flagging
        addBodyAddresses(message, addressBook, false);

        return STOP;
    }
}
