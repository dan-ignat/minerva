# Rules

## Auto-Reply Rules

- `DeliveryFailureRule`
    - Matches if `subject` has `"undeliverable"` or `"failure"`
    - Removes `body` addresses (flags if none)
    - Returns `STOP`

- `NoLongerHereRule`
    - Matches if `body` has `"no longer"` or `"any longer"`
    - Removes `from` address (flags if none)
    - Adds `body` addresses (flags if none)
    - Returns `STOP`

- `OutOfOfficeRule`
    - Matches if either:
        - `subject` has `"automatic reply"`, `"auto-reply"`, `"autoreply"`, `"auto reply"`, or `"auto"`
        - `subject`/`body` has `"out of office"`, `"out of the office"`, `"ooo"`, `"ooto"`, or `"vacation"`
    - Adds `body` addresses (no flag if none)
    - Returns `STOP`

- Flags if no rule matched

## Add Sender Rules

- `FlagAutoReplyRule`
    - Matches if `DeliveryFailureRule`, `NoLongerHereRule`, or `OutOfOfficeRule` matches
    - Flags
    - Returns `STOP`

- `AddSenderRule`
    - Matches always
    - Adds:
        - `from` address (flags if none)
        - `body` addresses (no flag if none)
    - Returns `PROCEED`

- Flags if no rule matched

## Remove Sender Rules

- `FlagAutoReplyRule`
    - Matches if `DeliveryFailureRule`, `NoLongerHereRule`, or `OutOfOfficeRule` matches
    - Flags
    - Returns `STOP`

- `RemoveSenderRule`
    - Matches always
    - Removes:
        - `from` address (flags if none)
        - `body` addresses (no flag if none)
    - Returns `PROCEED`

- Flags if no rule matched
