package name.ignat.minerva;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.NonFinal;
import name.ignat.minerva.model.AddressMatcher;

/**
 * Value classes that correspond to the run configuration file (YAML, validated with JSON Schema).
 * <p>
 * (These are not related to Spring {@code @Configuration} classes.
 * <p>
 * The following annotations are necessary to make Jackson deserialization work with immutable Lombok {@code @Value}
 * classes:
 * <pre>
 * {@code @AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)}
 * </pre>
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
    private List<SingleColumnSheetConfig> addressSheets;
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
// Since I need a custom all-args constructor to call the custom super constructor, I can't use these annotations here,
// and must instead use a custom no-args constructor too
//@AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
class AddressMatcherSheetConfig extends SingleColumnSheetConfig
{
    private AddressMatcher.Type type;

    @SuppressWarnings("unused")
    private AddressMatcherSheetConfig() { this(null, null, null); }

    public AddressMatcherSheetConfig(String name, String columnHeader, AddressMatcher.Type type)
    {
        super(name, columnHeader);
        this.type = type;
    }
}

@Value @NonFinal
@AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
class MessageFileConfig
{
    private String path;
    private ColumnHeadersConfig columnHeaders;

    @Value
    @AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
    static class ColumnHeadersConfig
    {
        private String from;
        private String subject;
        private String body;
    }
}

@Value @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
//Since I need a custom all-args constructor to call the custom super constructor, I can't use these annotations here,
//and must instead use a custom no-args constructor too
//@AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
class AddressMessageFileConfig extends MessageFileConfig
{
    private boolean extractBodyAddresses;

    @SuppressWarnings("unused")
    private AddressMessageFileConfig() { this(null, null, false); }

    public AddressMessageFileConfig(String path, ColumnHeadersConfig columnHeaders, boolean extractBodyAddresses)
    {
        super(path, columnHeaders);
        this.extractBodyAddresses = extractBodyAddresses;
    }
}

@Value @EqualsAndHashCode(callSuper = true) @ToString(callSuper = true)
// Since I need a custom all-args constructor to call the custom super constructor, I can't use these annotations here,
// and must instead use a custom no-args constructor too
//@AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
class MainMessageFileConfig extends MessageFileConfig
{
    private Type type;

    @SuppressWarnings("unused")
    private MainMessageFileConfig() { this(null, null, null); }

    public MainMessageFileConfig(String path, ColumnHeadersConfig columnHeaders, Type type)
    {
        super(path, columnHeaders);
        this.type = type;
    }

    enum Type { AUTO_REPLIES, ADD_SENDERS, REMOVE_SENDERS }
}

@Value
@AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
class OutputFileConfig
{
    private String path;
    private SingleColumnSheetConfig addressSheet;
    private MessageFlagSheetConfig messageFlagSheet;
    private AddressLogSheetConfig addressLogSheet;

    @Value
    @AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
    static class MessageFlagSheetConfig
    {
        private String name;
        private ColumnHeadersConfig columnHeaders;
    
        @Value
        @AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
        static class ColumnHeadersConfig
        {
            private String messageIndex;
            private String extractedAddresses;
            private String matchedRule;
            private String reason;
        }
    }

    @Value
    @AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
    static class AddressLogSheetConfig
    {
        private String name;
        private ColumnHeadersConfig columnHeaders;

        @Value
        @AllArgsConstructor @NoArgsConstructor(force = true, access = PRIVATE)
        static class ColumnHeadersConfig
        {
            private String messageIndex;
            private String action;
            private String address;
            private String extractedAddresses;
            private String matchedRule;
        }
    }
}
