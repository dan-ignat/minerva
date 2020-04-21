# Minerva

Minerva is a Core Java app that updates an e-mail address list based on filters, rules, and new messages.  It also flags
messages for manual analysis and audits how each address was handled.

## Input

1. Initial addresses
    - Provided via tabs in an Excel spreadsheet
2. Filters
    - Addresses, domains, or patterns (regular expressions)
    - Provided via tabs in an Excel spreadsheet
        - Addresses may also be provided via CSV files of exported messages
3. Messages
    - Auto-replies, messages from which to add addresses, or messages from which to remove addresses
    - Provided via CSV files of exported messages

## Output

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

### Run Config

Configuration of a Minerva run is done via a YAML file, which we often refer to as *run.yaml*, but it may have any name.

It is validated against *src/main/resources/run.schema.json*, using JSON Schema.

#### Example

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

#### General Structure

See [Run Config Details](doc/RunConfigDetails.md).

#### Sample Files

The *sample* dir contains sample input files that compose a fully working example that can be run to produce an output
file.  To run it:

```shell
> java --enable-preview -jar minerva.jar sample/run.yaml
```

### Rules

Minerva uses a simple rules engine to determine whether an e-mail message is matched and, if so, which addresses to
extract from it and what to do with them.

See [Rules](doc/Rules.md).

### Notes

For input Excel spreadsheets, the following formats are supported: *.xls*, *.xslx*, *.xslm*

## Architecture

Minerva is a Core Java app, using Spring Boot for bootstrapping and dependency injection.

Project Lombok is used to reduce boilerplate code (constructors, getters, setters, equals()/hashCode()/toString(),
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
