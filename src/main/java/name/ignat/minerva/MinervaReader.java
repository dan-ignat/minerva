package name.ignat.minerva;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import name.ignat.commons.exception.UnexpectedCaseException;
import name.ignat.minerva.io.read.csv.CsvReader;
import name.ignat.minerva.io.read.excel.ExcelReader;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.AddressFilters;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.address.AddressMatchers;
import name.ignat.minerva.model.address.AddressPattern;
import name.ignat.minerva.model.address.Domain;
import name.ignat.minerva.model.source.AddressMatcherSource;
import name.ignat.minerva.model.source.AddressSource;
import name.ignat.minerva.model.source.ContactFileSource;
import name.ignat.minerva.model.source.FilterMessageFileSource;
import name.ignat.minerva.util.Array;

@Component
public class MinervaReader
{
    private final MinervaRunConfig config;

    @Autowired
    public MinervaReader(MinervaRunConfig config)
    {
        this.config = config;
    }

    public AddressBook initAddressBook() throws IOException
    {
        ContactFileConfig contactFileConfig = config.getContactFile();

        if (contactFileConfig == null)
        {
            return new AddressBook();
        }

        File contactsFile = new File(contactFileConfig.getPath());

        try (ExcelReader contactsReader = new ExcelReader(contactsFile))
        {
            AddressFilters addressFilters = readAddressFilters(contactsReader);

            AddressBook addressBook = new AddressBook(addressFilters);

            List<InitialAddressSheetConfig> addressSheetConfigs = contactFileConfig.getAddressSheets();

            if (addressSheetConfigs != null)
            {
                for (InitialAddressSheetConfig addressSheetConfig : addressSheetConfigs)
                {
                    contactsReader.setSheetName(addressSheetConfig.getName());

                    List<Address> addresses =
                        contactsReader.read(Array.of(addressSheetConfig.getColumnHeader()), Address.class);

                    AddressSource source =
                        new ContactFileSource(contactsReader.getFile().getPath(), addressSheetConfig.getName());

                    addressBook.init(addresses, source, addressSheetConfig.isFilter());
                }
            }

            return addressBook;
        }
    }

    private AddressFilters readAddressFilters(ExcelReader contactsReader) throws IOException
    {
        AddressMatchers exclusionMatchers = readAddressMatchers(contactsReader,
            config.getContactFile().getExclusionSheets(), config.getExclusionMessageFiles());

        AddressMatchers flagMatchers = readAddressMatchers(contactsReader,
            config.getContactFile().getFlagSheets(), config.getFlagMessageFiles());

        return new AddressFilters(exclusionMatchers, flagMatchers);
    }

    private AddressMatchers readAddressMatchers(ExcelReader contactsReader,
        List<AddressMatcherSheetConfig> sheetConfigs, List<AddressMessageFileConfig> addressMessageFileConfigs)
        throws IOException
    {
        AddressMatchers.Builder builder = AddressMatchers.builder();

        if (sheetConfigs != null)
        {
            for (AddressMatcherSheetConfig sheetConfig : sheetConfigs)
            {
                contactsReader.setSheetName(sheetConfig.getName());

                String[] columnHeaders = Array.of(sheetConfig.getColumnHeader());

                AddressMatcherSource source =
                    new ContactFileSource(contactsReader.getFile().getPath(), sheetConfig.getName());

                switch (sheetConfig.getType())
                {
                    case ADDRESS:
                        builder.addAddresses(contactsReader.read(columnHeaders, Address.class), source);
                        break;
                    case DOMAIN:
                        builder.addDomains(contactsReader.read(columnHeaders, Domain.class), source);
                        break;
                    case PATTERN:
                        builder.addPatterns(contactsReader.read(columnHeaders, AddressPattern.class), source);
                        break;
                    default:
                        throw new UnexpectedCaseException(sheetConfig.getType());
                }
            }
        }

        if (addressMessageFileConfigs != null)
        {
            readMessageAddresses(addressMessageFileConfigs, builder);
        }

        return builder.build();
    }

    private void readMessageAddresses(
        List<AddressMessageFileConfig> addressMessageFileConfigs, AddressMatchers.Builder builder) throws IOException
    {
        for (AddressMessageFileConfig addressMessageFileConfig : addressMessageFileConfigs)
        {
            List<Message> addressMessages = readMessages(addressMessageFileConfig);

            for (Message addressMessage : addressMessages)
            {
                if (addressMessage.getFrom() != null)
                {
                    AddressMatcherSource source =
                        new FilterMessageFileSource(addressMessageFileConfig.getPath());

                    builder.addAddress(addressMessage.getFrom(), source);
                }

                if (addressMessageFileConfig.isExtractBodyAddresses())
                {
                    AddressMatcherSource source =
                        new FilterMessageFileSource(addressMessageFileConfig.getPath());

                    builder.addAddresses(addressMessage.getBodyAddresses(), source);
                }
            }
        }
    }

    public List<Message> readMessages(MessageFileConfig messageFileConfig) throws IOException
    {
        String messagesFilePath = messageFileConfig.getPath();

        File messagesFile = new File(messagesFilePath);

        try (CsvReader messagesReader = new CsvReader(messagesFile))
        {
            MessageFileConfig.ColumnHeadersConfig columnHeadersConfig = messageFileConfig.getColumnHeaders();

            String[] columnHeaders = Array.of(
                columnHeadersConfig.getFrom(), columnHeadersConfig.getSubject(), columnHeadersConfig.getBody());

            return messagesReader.read(columnHeaders, Message.class);
        }
    }
}
