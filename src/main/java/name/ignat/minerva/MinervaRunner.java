package name.ignat.minerva;

import static com.google.common.flogger.FluentLogger.forEnclosingClass;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.google.common.flogger.FluentLogger;

import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.rule.RuleEngine;

@Component
public class MinervaRunner implements CommandLineRunner
{
    private static final FluentLogger logger = forEnclosingClass();

    private final MinervaRunConfig config;
    private final MinervaReader reader;
    private final RuleEngine ruleEngine;
    private final MinervaWriter writer;

    @Autowired
    public MinervaRunner(MinervaRunConfig config, MinervaReader reader, RuleEngine ruleEngine, MinervaWriter writer)
    {
        this.config = config;
        this.reader = reader;
        this.ruleEngine = ruleEngine;
        this.writer = writer;
    }

    @Override
    public void run(String... args) throws IOException, IllegalAccessException
    {
        AddressBook addressBook = reader.initAddressBook();

        List<Message> messages = reader.readMessages(config.getMessageFile().getPath());

        ruleEngine.run(addressBook, messages);

        writer.writeAddressBook(addressBook);

        logger.atInfo().log("DONE!");
    }
}
