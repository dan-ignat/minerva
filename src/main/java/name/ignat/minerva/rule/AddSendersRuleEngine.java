package name.ignat.minerva.rule;

import name.ignat.minerva.rule.impl.AddSenderRule;
import name.ignat.minerva.rule.impl.FlagAutoReplyRule;

/**
 * @author Dan Ignat
 */
public class AddSendersRuleEngine extends RuleEngine
{
    public AddSendersRuleEngine(String messageFilePath)
    {
        super(
            new FlagAutoReplyRule(messageFilePath),
            new AddSenderRule(messageFilePath)
        );
    }
}
