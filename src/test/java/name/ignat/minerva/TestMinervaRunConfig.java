package name.ignat.minerva;

import static name.ignat.commons.utils.IoUtils.getClassPathResource;
import static name.ignat.minerva.MainMessageFileConfig.Type.ADD_SENDERS;
import static name.ignat.minerva.model.address.AddressMatcher.Type.ADDRESS;
import static name.ignat.minerva.model.address.AddressMatcher.Type.DOMAIN;
import static name.ignat.minerva.model.address.AddressMatcher.Type.PATTERN;
import static name.ignat.minerva.util.JacksonUtils.parseYaml;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import name.ignat.minerva.OutputFileConfig.AddressLogSheetConfig;
import name.ignat.minerva.OutputFileConfig.AddressSheetConfig;
import name.ignat.minerva.OutputFileConfig.MessageFlagSheetConfig;
import name.ignat.minerva.util.JacksonUtils.ValidationException;

public class TestMinervaRunConfig
{
    private static final String CONFIG_SCHEMA_FILE_PATH = "run.schema.json";

    private static Stream<Arguments> validCases()
    {
        return Stream.of(
            Arguments.of("TestMinervaRunConfig/Full-run.yaml",
                new MinervaRunConfig(
                    new ContactFileConfig(
                        "/path/to/Contacts.xlsx",
                        List.of(
                            new InitialAddressSheetConfig("Main",          "Address",  false),
                            new InitialAddressSheetConfig("MainOverrides", "Address2", true),
                            new InitialAddressSheetConfig("MainDefaults",  "Address",  false)
                        ),
                        List.of(
                            new AddressMatcherSheetConfig("Unsubscribed",          "Address",  ADDRESS),
                            new AddressMatcherSheetConfig("UnsubscribedOverrides", "Address2", ADDRESS),
                            new AddressMatcherSheetConfig("UnsubscribedDefaults",  "Address",  ADDRESS),
                            new AddressMatcherSheetConfig("Blacklisted",           "Domain",   DOMAIN),
                            new AddressMatcherSheetConfig("BlacklistedOverrides",  "Domain2",  DOMAIN),
                            new AddressMatcherSheetConfig("BlacklistedDefaults",   "Domain",   DOMAIN),
                            new AddressMatcherSheetConfig("Ignored",               "Pattern",  PATTERN),
                            new AddressMatcherSheetConfig("IgnoredOverrides",      "Pattern2", PATTERN),
                            new AddressMatcherSheetConfig("IgnoredDefaults",       "Pattern",  PATTERN)
                        ),
                        List.of(
                            new AddressMatcherSheetConfig("Colleagues",          "Address",  ADDRESS),
                            new AddressMatcherSheetConfig("ColleaguesOverrides", "Address2", ADDRESS),
                            new AddressMatcherSheetConfig("ColleaguesDefaults",  "Address",  ADDRESS),
                            new AddressMatcherSheetConfig("Personal",            "Domain",   DOMAIN),
                            new AddressMatcherSheetConfig("PersonalOverrides",   "Domain2",  DOMAIN),
                            new AddressMatcherSheetConfig("PersonalDefaults",    "Domain",   DOMAIN),
                            new AddressMatcherSheetConfig("Scam",                "Pattern",  PATTERN),
                            new AddressMatcherSheetConfig("ScamOverrides",       "Pattern2", PATTERN),
                            new AddressMatcherSheetConfig("ScamDefaults",        "Pattern",  PATTERN)
                        )
                    ),
                    List.of(
                        new AddressMessageFileConfig(
                            "/path/to/Exclusion Messages.csv",
                            new AddressMessageFileConfig.ColumnHeadersConfig("From: (Address)", "Subject", "Body"),
                            false
                        ),
                        new AddressMessageFileConfig(
                            "/path/to/Exclusion Messages Overrides.csv",
                            new AddressMessageFileConfig.ColumnHeadersConfig("From: (Address) 2", "Subject2", "Body2"),
                            true
                        ),
                        new AddressMessageFileConfig(
                            "/path/to/Exclusion Messages Defaults.csv",
                            new AddressMessageFileConfig.ColumnHeadersConfig("From: (Address)", "Subject", "Body"),
                            false
                        )
                    ),
                    List.of(
                        new AddressMessageFileConfig(
                            "/path/to/Flag Messages.csv",
                            new AddressMessageFileConfig.ColumnHeadersConfig("From: (Address)", "Subject", "Body"),
                            false
                        ),
                        new AddressMessageFileConfig(
                            "/path/to/Flag Messages Overrides.csv",
                            new AddressMessageFileConfig.ColumnHeadersConfig("From: (Address) 2", "Subject2", "Body2"),
                            true
                        ),
                        new AddressMessageFileConfig(
                            "/path/to/Flag Messages Defaults.csv",
                            new AddressMessageFileConfig.ColumnHeadersConfig("From: (Address)", "Subject", "Body"),
                            false
                        )
                    ),
                    new MainMessageFileConfig(
                        "/path/to/New Messages.csv",
                        new MessageFileConfig.ColumnHeadersConfig("From: (Address)", "Subject2", "Body"),
                        ADD_SENDERS
                    ),
                    new OutputFileConfig(
                        "/path/to/Contacts OUTPUT {dateTime} {messageFileType}.xlsx",
                        new AddressSheetConfig("Addresses2", "Address2"),
                        new MessageFlagSheetConfig(
                            "Message Flags2",
                            new MessageFlagSheetConfig.ColumnHeadersConfig("Index", "Matched Rule2", "Reason")
                        ),
                        new AddressLogSheetConfig(
                            "Address Log2",
                            new AddressLogSheetConfig.ColumnHeadersConfig(
                                "Message Index", "Action2", "Address", "Extracted Addresses2", "Matched Rule")
                        )
                    )
                )
            ),
            Arguments.of("TestMinervaRunConfig/Minimal-contactFile-addressSheets-run.yaml",
                new MinervaRunConfig(
                    new ContactFileConfig(
                        "/path/to/Contacts.xlsx",
                        List.of(
                            new InitialAddressSheetConfig("Main", "Address", false)
                        ),
                        null,
                        null
                    ),
                    null,
                    null,
                    new MainMessageFileConfig(
                        "/path/to/New Messages.csv",
                        new MessageFileConfig.ColumnHeadersConfig("From: (Address)", "Subject", "Body"),
                        ADD_SENDERS
                    ),
                    new OutputFileConfig(
                        "/path/to/Contacts OUTPUT {dateTime} {messageFileType}.xlsx",
                        new AddressSheetConfig("Addresses", "Address"),
                        new MessageFlagSheetConfig(
                            "Message Flags",
                            new MessageFlagSheetConfig.ColumnHeadersConfig("Index", "Matched Rule", "Reason")
                        ),
                        new AddressLogSheetConfig(
                            "Address Log",
                            new AddressLogSheetConfig.ColumnHeadersConfig(
                                "Message Index", "Action", "Address", "Extracted Addresses", "Matched Rule")
                        )
                    )
                )
            ),
            Arguments.of("TestMinervaRunConfig/Minimal-contactFile-exclusionSheets-run.yaml",
                new MinervaRunConfig(
                    new ContactFileConfig(
                        "/path/to/Contacts.xlsx",
                        null,
                        List.of(
                            new AddressMatcherSheetConfig("Unsubscribed", "Address", ADDRESS)
                        ),
                        null
                    ),
                    null,
                    null,
                    new MainMessageFileConfig(
                        "/path/to/New Messages.csv",
                        new MessageFileConfig.ColumnHeadersConfig("From: (Address)", "Subject", "Body"),
                        ADD_SENDERS
                    ),
                    new OutputFileConfig(
                        "/path/to/Contacts OUTPUT {dateTime} {messageFileType}.xlsx",
                        new AddressSheetConfig("Addresses", "Address"),
                        new MessageFlagSheetConfig(
                            "Message Flags",
                            new MessageFlagSheetConfig.ColumnHeadersConfig("Index", "Matched Rule", "Reason")
                        ),
                        new AddressLogSheetConfig(
                            "Address Log",
                            new AddressLogSheetConfig.ColumnHeadersConfig(
                                "Message Index", "Action", "Address", "Extracted Addresses", "Matched Rule")
                        )
                    )
                )
            ),
            Arguments.of("TestMinervaRunConfig/Minimal-contactFile-flagSheets-run.yaml",
                new MinervaRunConfig(
                    new ContactFileConfig(
                        "/path/to/Contacts.xlsx",
                        null,
                        null,
                        List.of(
                            new AddressMatcherSheetConfig("Colleagues", "Address", ADDRESS)
                        )
                    ),
                    null,
                    null,
                    new MainMessageFileConfig(
                        "/path/to/New Messages.csv",
                        new MessageFileConfig.ColumnHeadersConfig("From: (Address)", "Subject", "Body"),
                        ADD_SENDERS
                    ),
                    new OutputFileConfig(
                        "/path/to/Contacts OUTPUT {dateTime} {messageFileType}.xlsx",
                        new AddressSheetConfig("Addresses", "Address"),
                        new MessageFlagSheetConfig(
                            "Message Flags",
                            new MessageFlagSheetConfig.ColumnHeadersConfig("Index", "Matched Rule", "Reason")
                        ),
                        new AddressLogSheetConfig(
                            "Address Log",
                            new AddressLogSheetConfig.ColumnHeadersConfig(
                                "Message Index", "Action", "Address", "Extracted Addresses", "Matched Rule")
                        )
                    )
                )
            ),
            Arguments.of("TestMinervaRunConfig/Minimal-run.yaml",
                new MinervaRunConfig(
                    null,
                    null,
                    null,
                    new MainMessageFileConfig(
                        "/path/to/New Messages.csv",
                        new MessageFileConfig.ColumnHeadersConfig("From: (Address)", "Subject", "Body"),
                        ADD_SENDERS
                    ),
                    new OutputFileConfig(
                        "/path/to/Contacts OUTPUT {dateTime} {messageFileType}.xlsx",
                        new AddressSheetConfig("Addresses", "Address"),
                        new MessageFlagSheetConfig(
                            "Message Flags",
                            new MessageFlagSheetConfig.ColumnHeadersConfig("Index", "Matched Rule", "Reason")
                        ),
                        new AddressLogSheetConfig(
                            "Address Log",
                            new AddressLogSheetConfig.ColumnHeadersConfig(
                                "Message Index", "Action", "Address", "Extracted Addresses", "Matched Rule")
                        )
                    )
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("validCases")
    public void valid(String configFilePath, MinervaRunConfig expectedConfig) throws IOException
    {
        try (
            InputStream configSchemaFileIn = getClassPathResource(CONFIG_SCHEMA_FILE_PATH);
            InputStream configFileIn = getClassPathResource(configFilePath))
        {
            MinervaRunConfig config = parseYaml(configFileIn, MinervaRunConfig.class, configSchemaFileIn);

            assertThat(config, is(expectedConfig));
        }
    }

    private static Stream<Arguments> invalidCases()
    {
        return Stream.of(
            Arguments.of("TestMinervaRunConfig/Minimal-missing-messageFile-run.yaml"),
            Arguments.of("TestMinervaRunConfig/Minimal-missing-outputFile-run.yaml"),
            Arguments.of("TestMinervaRunConfig/Minimal-contactFile-incomplete-run.yaml")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidCases")
    public void invalid(String configFilePath) throws IOException
    {
        try (
            InputStream configSchemaFileIn = getClassPathResource(CONFIG_SCHEMA_FILE_PATH);
            InputStream configFileIn = getClassPathResource(configFilePath))
        {
            Executable executable = () -> parseYaml(configFileIn, MinervaRunConfig.class, configSchemaFileIn);

            assertThrows(ValidationException.class, executable);
        }
    }
}
