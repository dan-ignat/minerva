package name.ignat.minerva.io.write.csv;

import static com.google.common.collect.Streams.forEachPair;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.ADDED;
import static name.ignat.minerva.model.AuditLog.AddressEntry.Type.REMOVED;
import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.NO_BODY_ADDRESSES;
import static name.ignat.minerva.model.AuditLog.MessageFlag.Reason.UNEXPECTED_RULE_MATCHED;
import static name.ignat.minerva.util.StreamNextRest.streamNextRest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import name.ignat.minerva.io.write.WriteMapper;
import name.ignat.minerva.io.write.WriteMappers;
import name.ignat.minerva.model.Address;
import name.ignat.minerva.model.AuditLog.AddressEntry;
import name.ignat.minerva.model.AuditLog.MessageFlag;
import name.ignat.minerva.model.Domain;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.rule.impl.AddSenderRule;
import name.ignat.minerva.rule.impl.NoLongerHereRule;
import name.ignat.minerva.util.StreamNextRest;

public class TestCsvWriter
{
    private static Stream<Arguments> writeCases()
    {
        return Stream.of(
            Arguments.of(List.of("Address"), List.of(new Address("a@b.com"), new Address("b@b.com")), Address.class),

            Arguments.of(List.of("Domain"), List.of(new Domain("a.com"), new Domain("b.com")), Domain.class),

            Arguments.of(List.of("Index", "From", "Subject", "Body"),
                List.of(
                    new Message(2, "a@b.com", "Hello", "How's it going?"),
                    new Message(3, "b@b.com", "Hi", "Long time no see!")
                ),
                Message.class
            ),

            Arguments.of(List.of("Index", "Extracted Addresses", "Matched Rule", "Reason"),
                List.of(
                    new MessageFlag(
                        new Message(2, "a@b.com", "Hello", "How's it going?"),
                        new AddSenderRule(),
                        NO_BODY_ADDRESSES
                    ),
                    new MessageFlag(
                        new Message(3, "b@b.com", "Hi", "Long time no see!"),
                        new NoLongerHereRule(),
                        UNEXPECTED_RULE_MATCHED
                    )
                ),
                MessageFlag.class
            ),

            Arguments.of(List.of("Index", "Action", "E-mail Address", "Extracted Addresses", "Matched Rule"),
                List.of(
                    new AddressEntry(
                        ADDED,
                        new Address("a@b.com"),
                        new Message(2, "a@b.com", "Hello", "How's it going?"),
                        new AddSenderRule()
                    ),
                    new AddressEntry(
                        REMOVED,
                        new Address("b@b.com"),
                        new Message(3, "b@b.com", "Hi", "Long time no see!"),
                        new NoLongerHereRule()
                    )
                ),
                AddressEntry.class
            )
        );
    }

    @ParameterizedTest
    @MethodSource("writeCases")
    public <O> void write(List<String> columnHeaders, List<O> objects, Class<O> objectClass)
        throws IOException, CsvException
    {
        StringWriter stringWriter = new StringWriter();

        try (CsvWriter csvWriter = new CsvWriter(stringWriter))
        {
            csvWriter.write(columnHeaders.toArray(new String[0]), objects, objectClass);
        }

        Reader stringReader = new StringReader(stringWriter.toString());

        // Can't use CsvReader, because MessageFlags and AuditLog.Entrys don't have ReadMappers, and implementing them
        // would be non-trivial due to Messages being represented only by their indexes.
        /*
        List<O> readObjects;
        try (CsvReader csvReader = new CsvReader(stringReader))
        {
            readObjects = csvReader.read(columnHeaders.toArray(new String[0]), objectClass);
        }
        assertThat(readObjects, is(objects));
        */

        List<String[]> rows;
        try (CSVReader csvReader = new CSVReader(stringReader))
        {
            rows = csvReader.readAll();
        }

        StreamNextRest<String[]> rowsNextRest = streamNextRest(rows.stream());

        rowsNextRest.consumeNext(headerRow -> assertThat(headerRow, is(columnHeaders.toArray(new String[0]))));

        Stream<String[]> contentRowStream = rowsNextRest.getRest();

        WriteMapper<O> mapper = WriteMappers.forClass(objectClass);

        forEachPair(contentRowStream, objects.stream(),
            (contentRow, object) -> assertThat(contentRow, is(mapper.apply(object))));
    }
}
