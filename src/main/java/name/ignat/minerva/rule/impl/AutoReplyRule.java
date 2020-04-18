package name.ignat.minerva.rule.impl;

import lombok.EqualsAndHashCode;

/**
 * @author Dan Ignat
 */
@EqualsAndHashCode(callSuper = true)
public abstract class AutoReplyRule extends RuleBase
{
    public AutoReplyRule(String messageFilePath)
    {
        super(messageFilePath);
    }
}
