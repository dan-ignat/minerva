package name.ignat.minerva.rule.impl;

import lombok.EqualsAndHashCode;

/**
 * @author Dan Ignat
 */
@EqualsAndHashCode(callSuper = true)
public abstract class NormalReplyRule extends RuleBase
{
    public NormalReplyRule(String messageFilePath)
    {
        super(messageFilePath);
    }
}
