# Run Config Details

## General Structure

Notes:
- All leaf properties are strings, unless otherwise noted
- Leaf properties noted as optional have their default values in square brackets
- All `path` properties must be either absolute or relative to dir in which **java** is run
    - On Windows, absolute paths look like `/C:/some/path/...`

```yaml
# either contactFiles or messageFiles is required

contactFiles:                   # optional
    - path:                 ... # required; supported formats are .xlsx, .xlsm, .xls
      # either addressSheets, exclusionSheets, or flagSheets is required
      addressSheets:            # optional
        - name:             ... # required
          columnHeader:     ... # optional [Address]
          filter:           ... # optional [false]; boolean
        - ...
      exclusionSheets:          # optional
        - name:             ... # required
          type:             ... # required; {ADDRESS, DOMAIN, PATTERN} 
          columnHeader:     ... # optional [Address | Domain | Pattern, depending on `type`]
        - ...
      flagSheets:               # optional
        - name:             ... # required
          type:             ... # required; {ADDRESS, DOMAIN, PATTERN} 
          columnHeader:     ... # optional [Address | Domain | Pattern, depending on `type`]
        - ...
    - ...

exclusionMessageFiles:          # optional
    - path:                 ... # required; supported format is .csv
      columnHeaders:            # optional
        # one or more of the following are required
        from:               ... # optional [From: (Address)]
        subject:            ... # optional [Subject]
        body:               ... # optional [Body]
      extractBodyAddresses: ... # optional [false]; boolean
    - ...

flagMessageFiles:               # optional
    - path:                 ... # required; supported format is .csv
      columnHeaders:            # optional
        # one or more of the following are required
        from:               ... # optional [From: (Address)]
        subject:            ... # optional [Subject]
        body:               ... # optional [Body]
      extractBodyAddresses: ... # optional [false]; boolean
    - ...

messageFiles:                   # optional
    - type:                 ... # required; {AUTO_REPLIES, ADD_SENDERS, REMOVE_SENDERS}
      path:                 ... # required; supported format is .csv
      columnHeaders:            # optional
        # one or more of the following are required
        from:               ... # optional [From: (Address)]
        subject:            ... # optional [Subject]
        body:               ... # optional [Body]
    - ...

outputFile:                     # required
    # The expressions "{messageFileTypes}" and "{dateTime}" may be used in this `path` property, and
    # will be replaced accordingly at runtime
    path:                   ... # required; supported format is .xlsx
    addressSheet:               # optional
        # one or more of the following are required
        name:               ... # optional [Addresses]
        columnHeader:       ... # optional [Address]
    messageFlagSheet:           # optional
        # one or more of the following are required
        name:               ... # optional [Message Flags]
        columnHeaders:          # optional
            # one or more of the following are required
            messageIndex:   ... # optional [Index]
            matchedRule:    ... # optional [Matched Rule]
            reason:         ... # optional [Reason]
    addressLogSheet:            # optional
        # one or more of the following are required
        name:               ... # optional [Message Flags]
        columnHeaders:          # optional
            # one or more of the following are required
            messageIndex:   ... # optional [Message Index]
            address:        ... # optional [Address]
            addressSource:  ... # optional [Source]
            action:         ... # optional [Action]
            filterSources:  ... # optional [Filter Sources]
            matchedRule:    ... # optional [Matched Rule]
```
