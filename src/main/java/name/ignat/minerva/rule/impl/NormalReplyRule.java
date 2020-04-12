package name.ignat.minerva.rule.impl;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public abstract class NormalReplyRule extends RuleBase
{
    public NormalReplyRule(String messageFilePath)
    {
        super(messageFilePath);
    }
}
