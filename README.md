<figure>
    <img src="images/Minerva-800x294.jpg" alt="Statue of Minerva on the embankment at summer sunset; Castell de Tossa de Mar on Costa Brava, Catalunya, Spain.  (Photo by Kavalenkava Volha)">
    <figcaption><sup><em>Statue of Minerva on the embankment at summer sunset; Castell de Tossa de Mar on Costa Brava, Catalunya, Spain.  (Photo by Kavalenkava Volha)</em></sup></figcaption>
</figure>

# Minerva

Minerva is a Core Java app that updates an e-mail address list based on filters, rules, and new messages.  It also flags
messages for manual analysis and audits how each address was handled.

## Example

The best way to explain what Minerva can do is to look at some sample inputs and outputs.

### Inputs

Given these **Initial Addresses**:

| Address       |
| --------------|
| alice@a.com   |
| bob@b.com     |
| charlie@c.com |
| diane@d.com   |
| earl@e.com    |
| fred@f.com    |

... and these **Filters**:

| Unsubscribed | Blacklisted | Ignored | Personal Mail |
| ------------ | ----------- | ------- | ------------- |
| frank@f.com  | g.com       | /reply/ | gmail.com     |
|              |             |         | outlook.com   |

... and these **Auto-Replies**:

|     | From            | Subject                      | Body                                                                               |
| --- | --------------- | ---------------------------- | ---------------------------------------------------------------------------------- |
| 1   | Mailer Daemon   | Undeliverable: Touching base | Delivery has failed to these recipients or distribution lists: bob@b.com           |
| 2   | charlie@c.com   | Out of Office: Touching base | I'm on vacation until August 6th.  Please contact Carl at carl@c.com.  -Charlie    |
| 3   | earl@e.com      | Auto Reply: Touching base    | Earl is no longer with the company.  Instead, please contact Ethan at ethan@e.com. |
| 4   | oliver@o.com    | Hello                        | How are you?  (This isn't an auto-reply, so it should get flagged.)                |

... and these **New Messages**:

|     | From                   | Subject       | Body                                                                                 |
| --- | ---------------------- | ------------- | ------------------------------------------------------------------------------------ |
| 5   | mike@m.com             | Hello         | Hi!  Here's an opportunity that I thought would be a great fit for you.              |
| 6   | diane@d.com            | Checking in   | Hi!  It's been a while since we last spoke.  Is your contract wrapping up soon?      |
| 7   | frank@f.com            | Fulltime opp  | I have a great fulltime opp.  Let me know if you'd be interested in hearing more.    |
| 8   | george@g.com           | Terrible fit  | I know this has zero chances, but I'm gonna tell you about it anyway.                |
| 9   | hit-reply@linkedin.com | Hi            | If you want to network, feel free to send me your résumé at nancy@n.com.             |
| 10  | gene@gmail.com         | How you been? | Hey!  Haven't seen you since college.  How you been?                                 |

### Outputs

Minerva would produce this updated **Addresses** tab:

| Address       |
| ------------- |
| alice@a.com   |
| charlie@c.com |
| diane@d.com   |
| fred@f.com    |
| carl@c.com    |
| ethan@e.com   |
| mike@m.com    |
| nancy@n.com   |

... and this **Message Flags** tab:

| Index | Matched Rule | Reason          |
| ----- | ------------ | --------------- |
| 4     |              | NO_RULE_MATCHED |

... and this **Address Log** tab:

| Message Index | Address                | Source    | Action    | Filter Sources | Matched Rule        |
| ------------- | ---------------------- | --------- | --------- | -------------- | ------------------- |
|               | alice@a.com            | Addresses | ADDED     |                |                     |
|               | bob@b.com              | Addresses | ADDED     |                |                     |
|               | charlie@c.com          | Addresses | ADDED     |                |                     |
|               | diane@d.com            | Addresses | ADDED     |                |                     |
|               | earl@e.com             | Addresses | ADDED     |                |                     |
|               | fred@f.com             | Addresses | ADDED     |                |                     |
| 1             | bob@b.com              | BODY      | REMOVED   |                | DeliveryFailureRule |
| 2             | carl@c.com             | BODY      | ADDED     |                | OutOfOfficeRule     |
| 3             | earl@e.com             | FROM      | REMOVED   |                | NoLongerHereRule    |
| 3             | ethan@e.com            | BODY      | ADDED     |                | NoLongerHereRule    |
| 5             | mike@m.com             | FROM      | ADDED     |                | AddSenderRule       |
| 6             | diane@d.com            | FROM      | DUPLICATE |                | AddSenderRule       |
| 7             | frank@f.com            | FROM      | EXCLUDED  | Unsubscribed   | AddSenderRule       |
| 8             | george@g.com           | FROM      | EXCLUDED  | Blacklisted    | AddSenderRule       |
| 9             | hit-reply@linkedin.com | FROM      | EXCLUDED  | Ignored        | AddSenderRule       |
| 9             | nancy@n.com            | BODY      | ADDED     |                | AddSenderRule       |
| 10            | gene@gmail.com         | FROM      | FLAGGED   | Personal       | AddSenderRule       |

## Setup

Java 13 (JRE or JDK) is required to run the executable Minerva JAR.  The JAR is bootable, and therefore includes all of
Minerva's third-party dependencies.

## Usage

```shell
> java --enable-preview -jar path/to/minerva.jar path/to/run.yaml
```

All paths must be either absolute or relative to the directory from which you're running the `java` command.  This
includes:

- the path to *minerva.jar* (specified on the command line above)
- the path to *run.yaml* (specified on the command line above)
- the paths specified in *run.yaml*

On Windows, absolute paths look like `/C:/some/path/...`.

### Sample Files

The *samples* dir contains sample input files that compose a fully working example that can be run to produce an output
file.  To run it:

```shell
> java --enable-preview -jar minerva.jar samples/run.yaml
```

### Inputs

1. Initial addresses
    - Provided via tabs in an Excel spreadsheet
2. Filters
    - Addresses, domains, or patterns (regular expressions)
    - Provided via tabs in an Excel spreadsheet
        - Addresses may also be provided via CSV files of exported messages
3. Messages
    - Auto-replies, messages from which to add addresses, or messages from which to remove addresses
    - Provided via CSV files of exported messages

(For input Excel spreadsheets, the following formats are supported: *.xls*, *.xslx*, *.xslm*.)

### Outputs

An Excel spreadsheet (.xlsx) with the following tabs:

1. *Addresses*
    - A single column of addresses consisting of:
        - The initial addresses in their original order
        - Any new addresses in the order they were added by Minerva, for easy analysis
        - Minus any initial or new addresses that were removed by Minerva
2. *Message Flags*
    - A list of each message that was flagged for manual analysis, along with:
        - The rule that it matched
        - The reason it was flagged
3. *Address Log*
    - A log showing the outcome of each address that was analyzed, along with:
        - The index of the message it came from (if any)
        - The source of the address (message field or spreadsheet tab name)
        - The action that was taken on the address (Added, Duplicate, Excluded, Flagged, Removed, Remove Not Applicable)
        - The source of the matched filter (spreadsheet tab name or CSV file name), if the address was Excluded or Flagged
        - The rule that matched the message

### Run Config

Configuration of a Minerva run is done via a YAML file, which we often refer to as *run.yaml*, but it may have any name.

It is validated against *src/main/resources/run.schema.json*, using JSON Schema.

Example:

```yaml
contactFiles:
    - path: files/Contacts.xlsm
      addressSheets:
        - { name: Addresses }
      exclusionSheets:
        - { name: Unsubscribed, type: ADDRESS }
        - { name: Blacklisted,  type: DOMAIN  }
        - { name: Ignored,      type: PATTERN }
      flagSheets:
        - { name: Personal,     type: DOMAIN  }

exclusionMessageFiles:
    - path: files/Spam.csv

flagMessageFiles:
    - path: files/Colleagues.csv

messageFiles:
    - type: AUTO_REPLIES
      path: files/Auto Replies.csv
    - type: ADD_SENDERS
      path: files/New Messages.csv

outputFile:
    path: files/Contacts UPDATED {messageFileTypes} {dateTime}.xlsx
```

See [Run Config Details](docs/RunConfigDetails.md) for a complete reference.

### Rules

Minerva uses a simple rules engine to determine whether an e-mail message is matched and, if so, which addresses to
extract from it and what to do with them.

See [Rules](docs/Rules.md).

## Architecture

Minerva is a Core Java app, using Spring Boot for bootstrapping and dependency injection.

Project Lombok is used to reduce boilerplate code (constructors, getters, setters, `equals()`/`hashCode()`/`toString()`,
etc.).

YAML is used for user-specified configuration options (with Jackson for deserialization), and JSON Schema is used to
validate the YAML.

Google Guava and Apache Commons are used for utility code.

Apache POI is used to read/write Excel files, and OpenCSV is used to read/write CSV files.

Hamcrest is used for matcher-based assertions in both runtime code and test code.

Google Flogger is used for logging.

JUnit 5 is used for unit tests, mainly parameterized ones.

### Tech Stack

- Java 13
- Spring Boot
- Project Lombok
- YAML/JSON
- Jackson
- JSON Schema
- Google Guava
- Apache Commons (Lang3, I/O)
- Apache POI
- OpenCSV
- Hamcrest
- Google Flogger
- JUnit 5

## Design

TBD
