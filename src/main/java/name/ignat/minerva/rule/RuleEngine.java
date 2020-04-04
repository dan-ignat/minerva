package name.ignat.minerva.rule;

import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.NO_RULE_MATCHED;
import static name.ignat.minerva.rule.Rule.Signal.STOP;

import java.util.List;

import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.rule.Rule.Signal;

public abstract class RuleEngine
{
    private final Rule[] rules;

    /*
     * Rule order should be decided for optimal message matching, not to arrange additions/deletions, which should be
     * processed in the order that messages arrived in inbox.
     */
    public RuleEngine(Rule... rules)
    {
        this.rules = rules;
    }

    public void run(AddressBook addressBook, List<Message> messages)
    {
        messages:
        for (Message message : messages)
        {
            boolean ruleMatched = false;

            for (Rule rule : rules)
            {
                if (rule.matches(message, addressBook))
                {
                    ruleMatched = true;

                    Signal signal = rule.run(message, addressBook);

                    if (signal == STOP)
                    {
                        continue messages;
                    }
                }
            }

            if (!ruleMatched)
            {
                addressBook.flagMessage(message, null, NO_RULE_MATCHED);
            }
        }
    }
}
