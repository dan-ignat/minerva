package name.ignat.minerva.rule.impl;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class FlagAutoReplyRule extends FlagAnyMatchRule
{
    public FlagAutoReplyRule()
    {
        super(
            new DeliveryFailureRule(),
            new NoLongerHereRule(),
            new OutOfOfficeRule()
        );
    }
}
