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
import name.ignat.minerva.model.Message;
import name.ignat.minerva.model.address.Address;
import name.ignat.minerva.model.address.AddressMatchers;
import name.ignat.minerva.model.address.AddressPattern;
import name.ignat.minerva.model.address.Domain;
import name.ignat.minerva.model.source.ContactFileSource;
import name.ignat.minerva.model.source.FilterMessageFileSource;
import name.ignat.minerva.util.Array;

/**
 * @author Dan Ignat
 */
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
        AddressBook.Builder builder = AddressBook.builder();

        if (config.getContactFiles() != null)
        {
            for (ContactFileConfig contactFileConfig : config.getContactFiles())
            {
                File contactsFile = new File(contactFileConfig.getPath());

                try (ExcelReader contactsReader = new ExcelReader(contactsFile))
                {
                    readAddresses(contactFileConfig.getAddressSheets(), contactsReader, builder);

                    readAddressMatchers(
                        contactFileConfig.getExclusionSheets(), contactsReader, builder.exclusionMatchersBuilder());

                    readAddressMatchers(
                        contactFileConfig.getFlagSheets(), contactsReader, builder.flagMatchersBuilder());
                }
            }
        }

        readMessageAddresses(config.getExclusionMessageFiles(), builder.exclusionMatchersBuilder());

        readMessageAddresses(config.getFlagMessageFiles(), builder.flagMatchersBuilder());

        return builder.build();
    }

    private void readAddresses(List<InitialAddressSheetConfig> addressSheetConfigs, ExcelReader contactsReader,
        AddressBook.Builder builder) throws IOException
    {
        if (addressSheetConfigs != null)
        {
            for (InitialAddressSheetConfig addressSheetConfig : addressSheetConfigs)
            {
                contactsReader.setSheetName(addressSheetConfig.getName());

                List<Address> addresses =
                    contactsReader.read(Array.of(addressSheetConfig.getColumnHeader()), Address.class);

                ContactFileSource source =
                    new ContactFileSource(contactsReader.getFile().getPath(), addressSheetConfig.getName());

                builder.addInitial(addresses, source, addressSheetConfig.isFilter());
            }
        }
    }

    private void readAddressMatchers(List<AddressMatcherSheetConfig> sheetConfigs, ExcelReader contactsReader,
        AddressMatchers.Builder builder)
        throws IOException
    {
        if (sheetConfigs != null)
        {
            for (AddressMatcherSheetConfig sheetConfig : sheetConfigs)
            {
                contactsReader.setSheetName(sheetConfig.getName());

                String[] columnHeaders = Array.of(sheetConfig.getColumnHeader());

                ContactFileSource source =
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
    }

    private void readMessageAddresses(
        List<AddressMessageFileConfig> addressMessageFileConfigs, AddressMatchers.Builder builder) throws IOException
    {
        if (addressMessageFileConfigs != null)
        {
            for (AddressMessageFileConfig addressMessageFileConfig : addressMessageFileConfigs)
            {
                List<Message> addressMessages = readMessages(addressMessageFileConfig);

                for (Message addressMessage : addressMessages)
                {
                    FilterMessageFileSource source = new FilterMessageFileSource(addressMessageFileConfig.getPath());

                    if (addressMessage.getFrom() != null)
                    {
                        builder.addAddress(addressMessage.getFrom(), source);
                    }

                    if (addressMessageFileConfig.isExtractBodyAddresses())
                    {
                        builder.addAddresses(addressMessage.getBodyAddresses(), source);
                    }
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
