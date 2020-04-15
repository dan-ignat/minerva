package name.ignat.minerva;

import static name.ignat.commons.utils.IoUtils.getClassPathResource;
import static name.ignat.minerva.MinervaApp.CONFIG_SCHEMA_FILE_PATH;
import static name.ignat.minerva.util.JacksonUtils.parseYaml;
import static name.ignat.minerva.util.TestUtils.getNestedField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.collect.SetMultimap;

import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.source.AddressMatcherSource;

public class TestMinervaReader
{
    private static Stream<Arguments> initAddressBookCases()
    {
        return Stream.of(
            Arguments.of("TestMinervaReader/initAddressBook_twoContactFiles_twoFilterMessageFiles-run.yaml",
                List.of(
                    "a@b.com",  "b@b.com",
                    "a2@b.com", "b2@b.com"),
                List.of(
                    "c@d.com",  "d@d.com",
                    "c2@d.com", "d2@d.com",
                    "g@h.com",  "g@hh.com",  "h@h.com",  "h@hh.com",
                    "g2@h.com", "g2@hh.com", "h2@h.com", "h2@hh.com"),
                List.of(
                    "e@f.com",  "f@f.com",
                    "e2@f.com", "f2@f.com",
                    "i@j.com",  "i@jj.com",  "j@j.com",  "j@jj.com",
                    "i2@j.com", "i2@jj.com", "j2@j.com", "j2@jj.com")
            ),
            Arguments.of("TestMinervaReader/initAddressBook_badFrom_noBodyAddresses-run.yaml",
                List.of("a@b.com",  "b@b.com"),
                List.of(),
                List.of()
            )
        );
    }

    /*
     * Using ReflectionTestUtils for assertions here is easier and more maintainable than contriving a test case that
     * would apply each exclusion/flag on the initial addresses to produce a visible and assertable result.
     */
    @ParameterizedTest
    @MethodSource("initAddressBookCases")
    public void initAddressBook(String configFilePath,
        List<String> expectedAddressStrings, List<String> expectedExclusionAddressStrings,
        List<String> expectedFlagAddressStrings) throws IOException
    {
        try (
            InputStream configSchemaFileIn = getClassPathResource(CONFIG_SCHEMA_FILE_PATH);
            InputStream configFileIn = getClassPathResource(configFilePath))
        {
            MinervaRunConfig config = parseYaml(configFileIn, MinervaRunConfig.class, configSchemaFileIn);

            MinervaReader reader = new MinervaReader(config);

            // CALL UNDER TEST
            AddressBook addressBook = reader.initAddressBook();

            List<Address> expectedAddresses = Address.fromStrings(expectedAddressStrings);

            assertThat(List.copyOf(addressBook.getAddresses()), is(expectedAddresses));

            @SuppressWarnings("unchecked")
            Set<Address> exclusionAddresses = ((SetMultimap<Address, AddressMatcherSource>)
                getNestedField(addressBook, "addressFilters.exclusionMatchers.addresses")).keySet();

            List<Address> expectedExclusionAddresses = Address.fromStrings(expectedExclusionAddressStrings);

            assertThat(List.copyOf(exclusionAddresses), is(expectedExclusionAddresses));

            @SuppressWarnings("unchecked")
            Set<Address> flagAddresses = ((SetMultimap<Address, AddressMatcherSource>)
                getNestedField(addressBook, "addressFilters.flagMatchers.addresses")).keySet();

            List<Address> expectedFlagAddresses = Address.fromStrings(expectedFlagAddressStrings);

            assertThat(List.copyOf(flagAddresses), is(expectedFlagAddresses));
        }
    }
}
