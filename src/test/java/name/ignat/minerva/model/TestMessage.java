package name.ignat.minerva.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

public class TestMessage
{
    @Test
    public void badFrom()
    {
        Message message = new Message(
            2,
            "/O=EXCHANGELABS.../CN=MICROSOFTEXCHANGE...",
            "Hello",
            "Lorem ipsum dolor");

        assertThat(message.getFrom(), nullValue());

        assertThat(message.getFromRaw(), is("/O=EXCHANGELABS.../CN=MICROSOFTEXCHANGE..."));
    }
}
