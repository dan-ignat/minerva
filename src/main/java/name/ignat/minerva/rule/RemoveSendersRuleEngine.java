package name.ignat.minerva.rule;

import name.ignat.minerva.rule.impl.FlagAutoReplyRule;
import name.ignat.minerva.rule.impl.RemoveSenderRule;

/**
 * @author Dan Ignat
 */
public class RemoveSendersRuleEngine extends RuleEngine
{
    public RemoveSendersRuleEngine(String messageFilePath)
    {
        super(
            new FlagAutoReplyRule(messageFilePath),
            new RemoveSenderRule(messageFilePath)
        );
    }
}
