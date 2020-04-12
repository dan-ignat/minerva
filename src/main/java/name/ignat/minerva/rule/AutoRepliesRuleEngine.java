package name.ignat.minerva.rule;

import name.ignat.minerva.rule.impl.DeliveryFailureRule;
import name.ignat.minerva.rule.impl.NoLongerHereRule;
import name.ignat.minerva.rule.impl.OutOfOfficeRule;

public class AutoRepliesRuleEngine extends RuleEngine
{
    public AutoRepliesRuleEngine(String messageFilePath)
    {
        super(
            new DeliveryFailureRule(messageFilePath),
            new NoLongerHereRule(messageFilePath),
            new OutOfOfficeRule(messageFilePath)
        );
    }
}
