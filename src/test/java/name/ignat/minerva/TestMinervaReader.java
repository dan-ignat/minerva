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

import org.junit.jupiter.api.Test;

import com.google.common.collect.SetMultimap;

import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.source.AddressMatcherSource;

public class TestMinervaReader
{
    /*
     * Using ReflectionTestUtils for assertions here is easier and more maintainable than contriving a test case that
     * would apply each exclusion/flag on the initial addresses to produce a visible and assertable result.
     */
    @Test
    public void initAddressBook_twoContactFiles_twoFilterMessageFiles() throws IOException
    {
        String configFilePath = "TestMinervaReader/initAddressBook_twoContactFiles_twoFilterMessageFiles-run.yaml";

        try (
            InputStream configSchemaFileIn = getClassPathResource(CONFIG_SCHEMA_FILE_PATH);
            InputStream configFileIn = getClassPathResource(configFilePath))
        {
            MinervaRunConfig config = parseYaml(configFileIn, MinervaRunConfig.class, configSchemaFileIn);

            MinervaReader reader = new MinervaReader(config);

            // CALL UNDER TEST
            AddressBook addressBook = reader.initAddressBook();

            List<Address> expectedAddresses = Address.fromStrings(
                List.of(
                    "a@b.com",  "b@b.com",
                    "a2@b.com", "b2@b.com"));

            assertThat(List.copyOf(addressBook.getAddresses()), is(expectedAddresses));

            @SuppressWarnings("unchecked")
            Set<Address> exclusionAddresses = ((SetMultimap<Address, AddressMatcherSource>)
                getNestedField(addressBook, "addressFilters.exclusionMatchers.addresses")).keySet();

            List<Address> expectedExclusionAddresses = Address.fromStrings(
                List.of(
                    "c@d.com",  "d@d.com",
                    "c2@d.com", "d2@d.com",
                    "g@h.com",  "g@hh.com",  "h@h.com",  "h@hh.com",
                    "g2@h.com", "g2@hh.com", "h2@h.com", "h2@hh.com"));

            assertThat(List.copyOf(exclusionAddresses), is(expectedExclusionAddresses));

            @SuppressWarnings("unchecked")
            Set<Address> flagAddresses = ((SetMultimap<Address, AddressMatcherSource>)
                getNestedField(addressBook, "addressFilters.flagMatchers.addresses")).keySet();

            List<Address> expectedFlagAddresses = Address.fromStrings(
                List.of(
                    "e@f.com",  "f@f.com",
                    "e2@f.com", "f2@f.com",
                    "i@j.com",  "i@jj.com",  "j@j.com",  "j@jj.com",
                    "i2@j.com", "i2@jj.com", "j2@j.com", "j2@jj.com"));

            assertThat(List.copyOf(flagAddresses), is(expectedFlagAddresses));
        }
    }

    @Test
    public void initAddressBook_badFrom_noBodyAddresses() throws IOException
    {
        String configFilePath = "TestMinervaReader/initAddressBook_badFrom_noBodyAddresses-run.yaml";

        try (
            InputStream configSchemaFileIn = getClassPathResource(CONFIG_SCHEMA_FILE_PATH);
            InputStream configFileIn = getClassPathResource(configFilePath))
        {
            MinervaRunConfig config = parseYaml(configFileIn, MinervaRunConfig.class, configSchemaFileIn);

            MinervaReader reader = new MinervaReader(config);

            // CALL UNDER TEST
            AddressBook addressBook = reader.initAddressBook();

            List<Address> expectedAddresses = Address.fromStrings(
                List.of("a@b.com",  "b@b.com"));

            assertThat(List.copyOf(addressBook.getAddresses()), is(expectedAddresses));

            @SuppressWarnings("unchecked")
            Set<Address> exclusionAddresses = ((SetMultimap<Address, AddressMatcherSource>)
                getNestedField(addressBook, "addressFilters.exclusionMatchers.addresses")).keySet();

            List<Address> expectedExclusionAddresses = List.of();

            assertThat(List.copyOf(exclusionAddresses), is(expectedExclusionAddresses));

            @SuppressWarnings("unchecked")
            Set<Address> flagAddresses = ((SetMultimap<Address, AddressMatcherSource>)
                getNestedField(addressBook, "addressFilters.flagMatchers.addresses")).keySet();

            List<Address> expectedFlagAddresses = List.of();

            assertThat(List.copyOf(flagAddresses), is(expectedFlagAddresses));
        }
    }
}
