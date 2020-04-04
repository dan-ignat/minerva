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

@SpringBootTest(args = { "src/test/resources/TestAddRemoveSendersEndToEnd/Add-run.yaml" })
@Import(TestConfig.class)
@DirtiesContext
public class TestAddSendersEndToEnd extends TestBaseEndToEnd
{
    private static Stream<Arguments> runCase()
    {
        return Stream.of(
            Arguments.of(
                List.of(
                    List.of("E-mail Address"),
                    List.of("a@b.com"),
                    List.of("b@b.com"),
                    List.of("c@b.com"),
                    List.of("j@b.com")
                ),
                List.of(
                    List.of("Message Index", "Action",    "E-mail Address",         "Extracted Addresses", "Matched Rule"),
                    List.of("",              "ADDED",     "a@b.com",                "",                    ""),
                    List.of("",              "ADDED",     "b@b.com",                "",                    ""),
                    List.of("3",             "EXCLUDED",  "a@g.com",                "a@b.com",             "AddSenderRule"),
                    List.of("3",             "DUPLICATE", "a@b.com",                "a@b.com",             "AddSenderRule"),
                    List.of("4",             "EXCLUDED",  "hit-reply@linkedin.com", "c@b.com",             "AddSenderRule"),
                    List.of("4",             "ADDED",     "c@b.com",                "c@b.com",             "AddSenderRule"),
                    List.of("5",             "FLAGGED",   "d@gmail.com",            "e@outlook.com",       "AddSenderRule"),
                    List.of("5",             "FLAGGED",   "e@outlook.com",          "e@outlook.com",       "AddSenderRule"),
                    List.of("6",             "EXCLUDED",  "f@k.com",                "g@k.com",             "AddSenderRule"),
                    List.of("6",             "EXCLUDED",  "g@k.com",                "g@k.com",             "AddSenderRule"),
                    List.of("7",             "FLAGGED",   "h@m.com",                "i@m.com",             "AddSenderRule"),
                    List.of("7",             "FLAGGED",   "i@m.com",                "i@m.com",             "AddSenderRule"),
                    List.of("8",             "ADDED",     "j@b.com",                "",                    "AddSenderRule")
                ),
                List.of(
                    List.of("Index", "Extracted Addresses", "Matched Rule",        "Reason"),
                    List.of("2",     "a@b.com",             "DeliveryFailureRule", "UNEXPECTED_RULE_MATCHED"),
                    List.of("5",     "e@outlook.com",       "AddSenderRule",       "ADDRESS_FILTERS"),
                    List.of("5",     "e@outlook.com",       "AddSenderRule",       "ADDRESS_FILTERS"),
                    List.of("7",     "i@m.com",             "AddSenderRule",       "ADDRESS_FILTERS"),
                    List.of("7",     "i@m.com",             "AddSenderRule",       "ADDRESS_FILTERS")
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
