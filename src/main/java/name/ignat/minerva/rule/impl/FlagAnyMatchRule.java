package name.ignat.minerva.rule.impl;

import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.UNEXPECTED_RULE_MATCHED;
import static name.ignat.minerva.rule.Rule.Signal.STOP;

import lombok.EqualsAndHashCode;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.rule.Rule;

/**
 * @author Dan Ignat
 */
@EqualsAndHashCode(callSuper = true)
public class FlagAnyMatchRule extends RuleBase
{
    private Rule[] rules;

    public FlagAnyMatchRule(Rule... rules)
    {
        super(null);

        this.rules = rules;
    }

    @Override
    public boolean matches(Message message, AddressBook addressBook)
    {
        for (Rule rule : rules)
        {
            if (rule.matches(message, addressBook))
            {
                addressBook.flagMessage(message, rule, UNEXPECTED_RULE_MATCHED);

                return true;
            }
        }

        return false;
    }

    @Override
    public Signal run(Message message, AddressBook addressBook)
    {
        return STOP;
    }
}
