package name.ignat.minerva;

import static com.google.common.flogger.FluentLogger.forEnclosingClass;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.google.common.flogger.FluentLogger;

import name.ignat.commons.exception.UnexpectedCaseException;
import name.ignat.minerva.model.AddressBook;
import name.ignat.minerva.model.Message;
import name.ignat.minerva.rule.AddSendersRuleEngine;
import name.ignat.minerva.rule.AutoRepliesRuleEngine;
import name.ignat.minerva.rule.RemoveSendersRuleEngine;
import name.ignat.minerva.rule.RuleEngine;

/**
 * @author Dan Ignat
 */
@Component
public class MinervaRunner implements CommandLineRunner
{
    private static final FluentLogger logger = forEnclosingClass();

    private final MinervaRunConfig config;
    private final MinervaReader reader;
    private final MinervaWriter writer;

    @Autowired
    public MinervaRunner(MinervaRunConfig config, MinervaReader reader, MinervaWriter writer)
    {
        this.config = config;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public void run(String... args) throws IOException, IllegalAccessException
    {
        AddressBook addressBook = reader.initAddressBook();

        if (config.getMessageFiles() != null)
        {
            for (MainMessageFileConfig mainMessageFileConfig : config.getMessageFiles())
            {
                String mainMessageFilePath = mainMessageFileConfig.getPath();

                @SuppressWarnings("preview")
                RuleEngine ruleEngine = switch (mainMessageFileConfig.getType())
                {
                    case ADD_SENDERS: yield new AddSendersRuleEngine(mainMessageFilePath);
                    case AUTO_REPLIES: yield new AutoRepliesRuleEngine(mainMessageFilePath);
                    case REMOVE_SENDERS: yield new RemoveSendersRuleEngine(mainMessageFilePath);
                    default: throw new UnexpectedCaseException(mainMessageFileConfig.getType());
                };

                List<Message> messages = reader.readMessages(mainMessageFileConfig);

                ruleEngine.run(addressBook, messages);
            }
        }

        writer.writeAddressBook(addressBook);

        logger.atInfo().log("DONE!");
    }
}
