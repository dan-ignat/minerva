package name.ignat.minerva;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import name.ignat.commons.exception.UnexpectedCaseException;
import name.ignat.minerva.io.read.csv.CsvReader;
import name.ignat.minerva.io.read.excel.ExcelReader;
import name.ignat.minerva.model.Address;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.AddressFilters;
import name.ignat.minerva.model.AddressFilters.AddressMatchers;
import name.ignat.minerva.model.Domain;
import name.ignat.minerva.model.Message;
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

            List<SingleColumnSheetConfig> addressSheetConfigs = contactFileConfig.getAddressSheets();

            if (addressSheetConfigs != null)
            {
                for (SingleColumnSheetConfig addressSheetConfig : addressSheetConfigs)
                {
                    contactsReader.setSheetName(addressSheetConfig.getName());
    
                    List<Address> addresses =
                        contactsReader.read(Array.of(addressSheetConfig.getColumnHeader()), Address.class);
    
                    addressBook.init(addresses);
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
        Set<Address> addresses = new LinkedHashSet<>();
        Set<Domain> domains = new LinkedHashSet<>();
        Set<Pattern> patterns = new LinkedHashSet<>();

        if (sheetConfigs != null)
        {
            for (AddressMatcherSheetConfig sheetConfig : sheetConfigs)
            {
                contactsReader.setSheetName(sheetConfig.getName());

                String[] columnHeaders = Array.of(sheetConfig.getColumnHeader());

                switch (sheetConfig.getType())
                {
                    case ADDRESS:
                        addresses.addAll(contactsReader.read(columnHeaders, Address.class));
                        break;
                    case DOMAIN:
                        domains.addAll(contactsReader.read(columnHeaders, Domain.class));
                        break;
                    case PATTERN:
                        patterns.addAll(contactsReader.read(columnHeaders, Pattern.class));
                        break;
                    default:
                        throw new UnexpectedCaseException(sheetConfig.getType());
                }
            }
        }

        if (addressMessageFileConfigs != null)
        {
            addresses.addAll(readMessageAddresses(addressMessageFileConfigs));
        }

        return new AddressMatchers(addresses, domains, patterns);
    }

    private Set<Address> readMessageAddresses(
        List<AddressMessageFileConfig> addressMessageFileConfigs) throws IOException
    {
        Set<Address> messageAddresses = new LinkedHashSet<>();

        for (AddressMessageFileConfig addressMessageFileConfig : addressMessageFileConfigs)
        {
            List<Message> addressMessages = readMessages(addressMessageFileConfig.getPath());

            for (Message addressMessage : addressMessages)
            {
                messageAddresses.add(addressMessage.getFrom());

                if (addressMessageFileConfig.isExtractBodyAddresses())
                {
                    messageAddresses.addAll(addressMessage.getBodyAddresses());
                }
            }
        }

        return messageAddresses;
    }

    public List<Message> readMessages(String messagesFilePath) throws IOException
    {
        File messagesFile = new File(messagesFilePath);

        try (CsvReader messagesReader = new CsvReader(messagesFile))
        {
            AddressMessageFileConfig.ColumnHeadersConfig columnHeadersConfig = config.getMessageFile().getColumnHeaders();

            String[] columnHeaders = Array.of(
                columnHeadersConfig.getFrom(), columnHeadersConfig.getSubject(), columnHeadersConfig.getBody());

            return messagesReader.read(columnHeaders, Message.class);
        }
    }
}
