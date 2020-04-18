package name.ignat.minerva.rule.impl;

import lombok.EqualsAndHashCode;

/**
 * @author Dan Ignat
 */
@EqualsAndHashCode(callSuper = true)
public class FlagAutoReplyRule extends FlagAnyMatchRule
{
    public FlagAutoReplyRule(String messageFilePath)
    {
        super(
            new DeliveryFailureRule(messageFilePath),
            new NoLongerHereRule(messageFilePath),
            new OutOfOfficeRule(messageFilePath)
        );
    }
}
