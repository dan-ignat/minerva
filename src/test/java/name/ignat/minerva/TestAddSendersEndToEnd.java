package name.ignat.minerva;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import name.ignat.minerva.TestBaseEndToEnd.TestConfig;

/**
 * @author Dan Ignat
 */
@SpringBootTest(args = { "src/test/resources/TestEndToEnd/AddSenders/run.yaml" })
@Import(TestConfig.class)
@DirtiesContext
public class TestAddSendersEndToEnd extends TestBaseEndToEnd
{
    @Test
    public void run() throws IOException
    {
        super.run("TestEndToEnd/AddSenders/Expected Output.xlsx");
    }
}
