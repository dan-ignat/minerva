package name.ignat.minerva;

import static name.ignat.commons.utils.IoUtils.getClassPathResource;
import static name.ignat.minerva.MainMessageFileConfig.Type.ADD_SENDERS;
import static name.ignat.minerva.model.AddressMatcher.Type.ADDRESS;
import static name.ignat.minerva.model.AddressMatcher.Type.DOMAIN;
import static name.ignat.minerva.model.AddressMatcher.Type.PATTERN;
import static name.ignat.minerva.util.JacksonUtils.parseYaml;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
import name.ignat.minerva.OutputFileConfig.MessageFlagSheetConfig;
import name.ignat.minerva.util.JacksonUtils.ValidationException;

public class TestMinervaRunConfig
{
    private static final String CONFIG_SCHEMA_FILE_PATH = "run.schema.json";

    private static Stream<Arguments> fullCase()
    {
        return Stream.of(
            Arguments.of("TestMinervaRunConfig/Full-run.yaml",
                new MinervaRunConfig(
                    new ContactFileConfig(
                        "/path/to/Contacts.xlsx",
                        List.of(
                            new InitialAddressSheetConfig("Contract", "E-mail Address", true)
                        ),
                        List.of(
                            new AddressMatcherSheetConfig("Perm", "E-mail Address", ADDRESS),
                            new AddressMatcherSheetConfig("Unsubscribed", "E-mail Address", ADDRESS),
                            new AddressMatcherSheetConfig("Blacklisted", "Domain", DOMAIN),
                            new AddressMatcherSheetConfig("Ignored", "Domain", DOMAIN),
                            new AddressMatcherSheetConfig("Ignored Patterns", "Pattern", PATTERN)
                        ),
                        List.of(
                            new AddressMatcherSheetConfig("Personal Domains", "Domain", DOMAIN)
                        )
                    ),
                    List.of(
                        new AddressMessageFileConfig(
                            "/path/to/To Be Unsubscribed.csv",
                            new AddressMessageFileConfig.ColumnHeadersConfig("From: (Address)", "Subject", "Body"),
                            true
                        )
                    ),
                    List.of(
                        new AddressMessageFileConfig(
                            "/path/to/Colleagues.csv",
                            new AddressMessageFileConfig.ColumnHeadersConfig("From: (Address)", "Subject", "Body"),
                            false
                        )
                    ),
                    new MainMessageFileConfig(
                        "/path/to/New Messages.csv",
                        new MessageFileConfig.ColumnHeadersConfig("From: (Address)", "Subject", "Body"),
                        ADD_SENDERS
                    ),
                    new OutputFileConfig(
                        "/path/to/Contacts UPDATED {dateTime}.xlsx",
                        new SingleColumnSheetConfig("Contract", "E-mail Address"),
                        new MessageFlagSheetConfig(
                            "Flagged Messages",
                            new MessageFlagSheetConfig.ColumnHeadersConfig(
                                "Index", "Extracted Addresses", "Matched Rule", "Reason")
                        ),
                        new AddressLogSheetConfig(
                            "Address Log",
                            new AddressLogSheetConfig.ColumnHeadersConfig(
                                "Message Index", "Action", "E-mail Address", "Extracted Addresses", "Matched Rule")
                        )
                    )
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("fullCase")
    public void full(String configFilePath, MinervaRunConfig expectedConfig) throws IOException
    {
        try (
            InputStream configSchemaFileIn = getClassPathResource(CONFIG_SCHEMA_FILE_PATH);
            InputStream configFileIn = getClassPathResource(configFilePath))
        {
            MinervaRunConfig config = parseYaml(configFileIn, MinervaRunConfig.class, configSchemaFileIn);

            assertThat(config, is(expectedConfig));
        }
    }

    private static Stream<Arguments> parseCases()
    {
        return Stream.of(
            Arguments.of("TestMinervaRunConfig/Minimal-run.yaml",                             true),
            Arguments.of("TestMinervaRunConfig/Minimal-missing-messageFile-run.yaml",         false),
            Arguments.of("TestMinervaRunConfig/Minimal-missing-outputFile-run.yaml",          false),
            Arguments.of("TestMinervaRunConfig/Minimal-contactFile-addressSheets-run.yaml",   true),
            Arguments.of("TestMinervaRunConfig/Minimal-contactFile-exclusionSheets-run.yaml", true),
            Arguments.of("TestMinervaRunConfig/Minimal-contactFile-flagSheets-run.yaml",      true),
            Arguments.of("TestMinervaRunConfig/Minimal-contactFile-incomplete-run.yaml",      false)
        );
    }

    @ParameterizedTest
    @MethodSource("parseCases")
    public void parse(String configFilePath, boolean expectedValid) throws IOException
    {
        try (
            InputStream configSchemaFileIn = getClassPathResource(CONFIG_SCHEMA_FILE_PATH);
            InputStream configFileIn = getClassPathResource(configFilePath))
        {
            Executable executable = () -> parseYaml(configFileIn, MinervaRunConfig.class, configSchemaFileIn);

            if (expectedValid)
            {
                assertDoesNotThrow(executable);
            }
            else
            {
                assertThrows(ValidationException.class, executable);
            }
        }
    }
}
