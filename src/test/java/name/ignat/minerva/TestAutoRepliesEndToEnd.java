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

@SpringBootTest(args = { "src/test/resources/TestAutoRepliesEndToEnd/run.yaml" })
@Import(TestConfig.class)
@DirtiesContext
public class TestAutoRepliesEndToEnd extends TestBaseEndToEnd
{
    private static Stream<Arguments> runCase()
    {
        return Stream.of(
            Arguments.of(
                List.of(
                    List.of("E-mail Address"),
                    List.of("c@b.com"),
                    List.of("d@b.com")
                ),
                List.of(
                    List.of("Message Index", "Action",    "E-mail Address",  "Extracted Addresses", "Matched Rule"),
                    List.of("",              "ADDED",     "a@b.com",         "",                    ""),
                    List.of("",              "ADDED",     "b@b.com",         "",                    ""),
                    List.of("2",             "REMOVED",   "a@b.com",         "a@b.com",             "DeliveryFailureRule"),
                    List.of("3",             "REMOVED",   "b@b.com",         "c@b.com",             "NoLongerHereRule"),
                    List.of("3",             "ADDED",     "c@b.com",         "c@b.com",             "NoLongerHereRule"),
                    List.of("4",             "ADDED",     "d@b.com",         "d@b.com",             "OutOfOfficeRule")
                ),
                List.of(
                    List.of("Index", "Matched Rule", "Reason")
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
