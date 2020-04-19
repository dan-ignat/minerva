# Minerva

Minerva generates an updated e-mail address list based on an initial list, filters, and addresses extracted from new
e-mail messages using various rules.

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

```
> java --enable-preview -jar path/to/minerva.jar path/to/run.yaml
```

All paths must be either absolute or relative to the directory from which you're running the `java` command.  This
includes:

- the path to *minerva.jar* (specified on the command line above)
- the path to *run.yaml* (specified on the command line above)
- the paths specified in *run.yaml*

### Run config

Configuration of a Minerva run is done via a YAML file, which we refer to as *run.yaml*, but it may have any name.

It is validated against *src/main/resources/run.schema.json*, using JSON Schema.

#### Example

```
contactFiles:
    - path: files/Contacts.xlsm
      addressSheets:
        - { name: Addresses }
      exclusionSheets:
        - { name: Unsubscribed,     type: ADDRESS }
        - { name: Blacklisted,      type: DOMAIN  }
        - { name: Ignored,          type: DOMAIN  }
        - { name: Ignored Patterns, type: PATTERN }
      flagSheets:
        - { name: Personal Domains, type: DOMAIN  }

exclusionMessageFiles:
    - path: files/Spam.csv

flagMessageFiles:
    - path: files/Colleagues.csv

messageFiles:
    - type: AUTO_REPLIES
      path: files/Auto Replies.csv
    - type: ADD_SENDERS
      path: files/New Messages.csv
    #- type: REMOVE_SENDERS
    #  path: files/Recent Messages.csv

outputFile:
    path: files/Contacts UPDATED {messageFileTypes} {dateTime}.xlsx
```

#### General Structure

```
```

### Notes

For input Excel spreadsheets, the following formats are supported: *.xls*, *.xslx*, *.xslm*

## Architecture

TBD

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
