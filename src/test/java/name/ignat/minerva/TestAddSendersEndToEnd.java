package name.ignat.minerva;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import name.ignat.minerva.TestBaseEndToEnd.TestConfig;

@SpringBootTest(args = { "src/test/resources/TestEndToEnd/AddSenders-run.yaml" })
@Import(TestConfig.class)
@DirtiesContext
public class TestAddSendersEndToEnd extends TestBaseEndToEnd
{
    private static Stream<Arguments> runCase()
    {
        return Stream.of(
            Arguments.of(
                List.of(
                    List.of("Address"),
                    List.of("a@b.com"),
                    List.of("b@b.com"),
                    List.of("c@b.com"),
                    List.of("j@b.com")
                ),
                List.of(
                    List.of("Message Index", "Address",                "Source",   "Action",    "Filter Sources",                                         "Matched Rule"),
                    List.of("",              "a@b.com",                "Contract", "ADDED",     "",                                                       ""),
                    List.of("",              "b@b.com",                "Contract", "ADDED",     "",                                                       ""),
                    List.of("3",             "a@g.com",                "FROM",     "EXCLUDED",  "Blacklisted",                                            "AddSenderRule"),
                    List.of("3",             "a@b.com",                "BODY",     "DUPLICATE", "",                                                       "AddSenderRule"),
                    List.of("4",             "hit-reply@linkedin.com", "FROM",     "EXCLUDED",  "Ignored Patterns",                                       "AddSenderRule"),
                    List.of("4",             "c@b.com",                "BODY",     "ADDED",     "",                                                       "AddSenderRule"),
                    List.of("5",             "d@gmail.com",            "FROM",     "FLAGGED",   "Personal Domains",                                       "AddSenderRule"),
                    List.of("5",             "e@outlook.com",          "BODY",     "FLAGGED",   "Personal Domains",                                       "AddSenderRule"),
                    List.of("6",             "f@k.com",                "FROM",     "EXCLUDED",  "src/test/resources/TestEndToEnd/To Be Unsubscribed.csv", "AddSenderRule"),
                    List.of("6",             "g@k.com",                "BODY",     "EXCLUDED",  "src/test/resources/TestEndToEnd/To Be Unsubscribed.csv", "AddSenderRule"),
                    List.of("7",             "h@m.com",                "FROM",     "FLAGGED",   "src/test/resources/TestEndToEnd/Colleagues.csv",         "AddSenderRule"),
                    List.of("7",             "i@m.com",                "BODY",     "FLAGGED",   "src/test/resources/TestEndToEnd/Colleagues.csv",         "AddSenderRule"),
                    List.of("8",             "j@b.com",                "FROM",     "ADDED",     "",                                                       "AddSenderRule")
                ),
                List.of(
                    List.of("Index", "Matched Rule",        "Reason"),
                    List.of("2",     "DeliveryFailureRule", "UNEXPECTED_RULE_MATCHED")
                )
            )
        );
    }

    @ParameterizedTest
    @MethodSource("runCase")
    public void run(List<List<String>> expectedAddressRows, List<List<String>> expectedAddressLogRows,
        List<List<String>> expectedMessageFlagRows) throws IOException
    {
        super.run(expectedAddressRows, expectedAddressLogRows, expectedMessageFlagRows);
    }
}
