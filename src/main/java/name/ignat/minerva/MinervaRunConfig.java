package name.ignat.minerva;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import name.ignat.commons.exception.UnexpectedCaseException;
import name.ignat.minerva.model.address.AddressMatcher;

/**
 * Value classes that correspond to the run configuration file (YAML, validated with JSON Schema).
 * <p>
 * (These are unrelated to Spring {@code @Configuration} classes.
 * 
 * @implNote
 * The following annotations are necessary to make Jackson deserialization work with immutable Lombok {@code @Value}
 * classes:
 * <pre>
 * {@code @AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)}
 * </pre>
 * If a {@code @Value} class needs to call a non-default super constructor, both of the above constructors must be
 * custom-coded.  If a {@code @Value} class needs to provide default values, the no-arg constructor must be custom-coded
 * with the defaults passed to the generated {@code @AllArgsConstructor}.
 * <p>
 * The following annotations are necessary to allow extending immutable Lombok {@code @Value} classes:
 * <pre>
 * {@code @NonFinal}
 * </pre>
 * <pre>
 * {@code @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)}
 * </pre>
 * 
 * @see https://stackoverflow.com/questions/39381474/cant-make-jackson-and-lombok-work-together
 * 
 * @author Dan Ignat
 */
@Value
@AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
public class MinervaRunConfig
{
    private ContactFileConfig contactFile;
    private List<AddressMessageFileConfig> exclusionMessageFiles;
    private List<AddressMessageFileConfig> flagMessageFiles;
    private MainMessageFileConfig messageFile;
    private OutputFileConfig outputFile;
}

@Value
@AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
class ContactFileConfig
{
    private String path;
    private List<InitialAddressSheetConfig> addressSheets;
    private List<AddressMatcherSheetConfig> exclusionSheets;
    private List<AddressMatcherSheetConfig> flagSheets;
}

@Value @NonFinal
@AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
class SingleColumnSheetConfig
{
    private String name;
    private String columnHeader;
}

@Value @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
class InitialAddressSheetConfig extends SingleColumnSheetConfig
{
    private static final String DEFAULT_COLUMN_HEADER = "Address";
    private static final boolean DEFAULT_FILTER = false;

    private boolean filter;

    @SuppressWarnings("unused")
    private InitialAddressSheetConfig() { this(null, DEFAULT_COLUMN_HEADER, DEFAULT_FILTER); }

    public InitialAddressSheetConfig(String name, String columnHeader, boolean filter)
    {
        super(name, columnHeader);
        this.filter = filter;
    }
}

@Value @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
class AddressMatcherSheetConfig extends SingleColumnSheetConfig
{
    private static final String DEFAULT_COLUMN_HEADER_ADDRESS = "Address";
    private static final String DEFAULT_COLUMN_HEADER_DOMAIN = "Domain";
    private static final String DEFAULT_COLUMN_HEADER_PATTERN = "Pattern";

    private AddressMatcher.Type type;

    // Unlike other Config classes, this one needs a dynamic default based on {@code type}, so it has to do the
    // defaulting below in {@code getColumnHeader()}
    @SuppressWarnings("unused")
    private AddressMatcherSheetConfig() { this(null, null, null); }

    public AddressMatcherSheetConfig(String name, String columnHeader, AddressMatcher.Type type)
    {
        super(name, columnHeader);
        this.type = type;
    }

    // Dynamic defaulting based on {@code type}
    @Override
    public String getColumnHeader()
    {
        String columnHeader = super.getColumnHeader();

        if (columnHeader != null)
        {
            return columnHeader;
        }

        @SuppressWarnings("preview")
        String defaultColumnHeader = switch (type)
        {
            case ADDRESS: yield DEFAULT_COLUMN_HEADER_ADDRESS;
            case DOMAIN:  yield DEFAULT_COLUMN_HEADER_DOMAIN;
            case PATTERN: yield DEFAULT_COLUMN_HEADER_PATTERN;
            default: throw new UnexpectedCaseException(type);
        };

        return defaultColumnHeader;
    }
}

@Value @NonFinal
@AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
class MessageFileConfig
{
    private String path;
    private ColumnHeadersConfig columnHeaders;

    @Value
    @AllArgsConstructor
    static class ColumnHeadersConfig
    {
        private static final String DEFAULT_FROM    = "From: (Address)";
        private static final String DEFAULT_SUBJECT = "Subject";
        private static final String DEFAULT_BODY    = "Body";

        ColumnHeadersConfig() { this(DEFAULT_FROM, DEFAULT_SUBJECT, DEFAULT_BODY); }

        private String from;
        private String subject;
        private String body;
    }
}

@Value @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
class AddressMessageFileConfig extends MessageFileConfig
{
    private static final boolean DEFAULT_EXTRACT_BODY_ADDRESSES = false;

    private boolean extractBodyAddresses;

    @SuppressWarnings("unused")
    private AddressMessageFileConfig() { this(null, new ColumnHeadersConfig(), DEFAULT_EXTRACT_BODY_ADDRESSES); }

    public AddressMessageFileConfig(String path, ColumnHeadersConfig columnHeaders, boolean extractBodyAddresses)
    {
        super(path, columnHeaders);
        this.extractBodyAddresses = extractBodyAddresses;
    }
}

@Value @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
class MainMessageFileConfig extends MessageFileConfig
{
    private Type type;

    @SuppressWarnings("unused")
    private MainMessageFileConfig() { this(null, new ColumnHeadersConfig(), null); }

    public MainMessageFileConfig(String path, ColumnHeadersConfig columnHeaders, Type type)
    {
        super(path, columnHeaders);
        this.type = type;
    }

    enum Type { AUTO_REPLIES, ADD_SENDERS, REMOVE_SENDERS }
}

@Value
@AllArgsConstructor
class OutputFileConfig
{
    private String path;
    private AddressSheetConfig addressSheet;
    private MessageFlagSheetConfig messageFlagSheet;
    private AddressLogSheetConfig addressLogSheet;

    @SuppressWarnings("unused")
    private OutputFileConfig()
    {
        this(null, new AddressSheetConfig(), new MessageFlagSheetConfig(), new AddressLogSheetConfig());
    }

    @Value @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
    static class AddressSheetConfig extends SingleColumnSheetConfig
    {
        private static final String DEFAULT_NAME = "Addresses";
        private static final String DEFAULT_COLUMN_HEADER = "Address";

        private AddressSheetConfig() { this(DEFAULT_NAME, DEFAULT_COLUMN_HEADER); }

        public AddressSheetConfig(String name, String columnHeader)
        {
            super(name, columnHeader);
        }
    }

    @Value
    @AllArgsConstructor
    static class MessageFlagSheetConfig
    {
        private static final String DEFAULT_NAME = "Message Flags";

        private String name;
        private ColumnHeadersConfig columnHeaders;
    
        private MessageFlagSheetConfig() { this(DEFAULT_NAME, new ColumnHeadersConfig()); }

        @Value
        @AllArgsConstructor
        static class ColumnHeadersConfig
        {
            private static final String DEFAULT_MESSAGE_INDEX = "Index";
            private static final String DEFAULT_MATCHED_RULE  = "Matched Rule";
            private static final String DEFAULT_REASON        = "Reason";

            private ColumnHeadersConfig() { this(DEFAULT_MESSAGE_INDEX, DEFAULT_MATCHED_RULE, DEFAULT_REASON); }

            private String messageIndex;
            private String matchedRule;
            private String reason;
        }
    }

    @Value
    @AllArgsConstructor
    static class AddressLogSheetConfig
    {
        private static final String DEFAULT_NAME = "Address Log";

        private String name;
        private ColumnHeadersConfig columnHeaders;

        private AddressLogSheetConfig() { this(DEFAULT_NAME, new ColumnHeadersConfig()); }

        @Value
        @AllArgsConstructor
        static class ColumnHeadersConfig
        {
            private static final String DEFAULT_MESSAGE_INDEX       = "Message Index";
            private static final String DEFAULT_ADDRESS             = "Address";
            private static final String DEFAULT_ADDRESS_SOURCE      = "Source";
            private static final String DEFAULT_ACTION              = "Action";
            private static final String DEFAULT_FILTER_SOURCES      = "Filter Sources";
            private static final String DEFAULT_MATCHED_RULE        = "Matched Rule";

            private ColumnHeadersConfig() { this(DEFAULT_MESSAGE_INDEX, DEFAULT_ADDRESS, DEFAULT_ADDRESS_SOURCE,
                DEFAULT_ACTION, DEFAULT_FILTER_SOURCES, DEFAULT_MATCHED_RULE); }

            private String messageIndex;
            private String address;
            private String addressSource;
            private String action;
            private String filterSources;
            private String matchedRule;
        }
    }
}
