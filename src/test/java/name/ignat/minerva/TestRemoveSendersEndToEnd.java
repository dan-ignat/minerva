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

@SpringBootTest(args = { "src/test/resources/TestAddRemoveSendersEndToEnd/Remove-run.yaml" })
@Import(TestConfig.class)
@DirtiesContext
public class TestRemoveSendersEndToEnd extends TestBaseEndToEnd
{
    private static Stream<Arguments> runCase()
    {
        return Stream.of(
            Arguments.of(
                List.of(
                    List.of("E-mail Address"),
                    List.of("b@b.com")
                ),
                List.of(
                    List.of("Message Index", "Action",    "E-mail Address",         "Extracted Addresses", "Matched Rule"),
                    List.of("",              "ADDED",     "a@b.com",                "",                    ""),
                    List.of("",              "ADDED",     "b@b.com",                "",                    ""),
                    List.of("3",             "REMOVED",   "a@b.com",                "a2@b.com",            "RemoveSenderRule"),
                    List.of("3",             "REMOVE_NA", "a2@b.com",               "a2@b.com",            "RemoveSenderRule")
                ),
                List.of(
                    List.of("Index", "Extracted Addresses", "Matched Rule",        "Reason"),
                    List.of("2",     "a@b.com",             "DeliveryFailureRule", "UNEXPECTED_RULE_MATCHED")
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
