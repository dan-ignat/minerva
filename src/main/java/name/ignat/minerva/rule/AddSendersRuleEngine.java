package name.ignat.minerva.rule;

import name.ignat.minerva.rule.impl.AddSenderRule;
import name.ignat.minerva.rule.impl.FlagAutoReplyRule;

public class AddSendersRuleEngine extends RuleEngine
{
    public AddSendersRuleEngine()
    {
        super(
            new FlagAutoReplyRule(),
            new AddSenderRule()
        );
    }
}
