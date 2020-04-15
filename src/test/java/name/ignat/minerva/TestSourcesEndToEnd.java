package name.ignat.minerva;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import name.ignat.minerva.TestBaseEndToEnd.TestConfig;

@SpringBootTest(args = { "src/test/resources/TestEndToEnd/Sources/run.yaml" })
@Import(TestConfig.class)
@DirtiesContext
public class TestSourcesEndToEnd extends TestBaseEndToEnd
{
    @Test
    public void run() throws IOException
    {
        super.run("TestEndToEnd/Sources/Expected Output.xlsx");
    }
}
