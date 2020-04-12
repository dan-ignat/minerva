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

@SpringBootTest(args = { "src/test/resources/TestEndToEnd/AutoReplies-run.yaml" })
@Import(TestConfig.class)
@DirtiesContext
public class TestAutoRepliesEndToEnd extends TestBaseEndToEnd
{
    private static Stream<Arguments> runCase()
    {
        return Stream.of(
            Arguments.of(
                List.of(
                    List.of("Address"),
                    List.of("c@b.com"),
                    List.of("d@b.com")
                ),
                List.of(
                    List.of("Message Index", "Address", "Source",   "Action",  "Filter Sources", "Matched Rule"),
                    List.of("",              "a@b.com", "Contract", "ADDED",   "",               ""),
                    List.of("",              "b@b.com", "Contract", "ADDED",   "",               ""),
                    List.of("2",             "a@b.com", "BODY",     "REMOVED", "",               "DeliveryFailureRule"),
                    List.of("3",             "b@b.com", "FROM",     "REMOVED", "",               "NoLongerHereRule"),
                    List.of("3",             "c@b.com", "BODY",     "ADDED",   "",               "NoLongerHereRule"),
                    List.of("4",             "d@b.com", "BODY",     "ADDED",   "",               "OutOfOfficeRule")
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
