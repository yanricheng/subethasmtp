package org.subethamail.smtp.netty.cmd.impl;

import org.subethamail.smtp.internal.server.CommandException;
import org.subethamail.smtp.netty.SMTPServerConfig;
import org.subethamail.smtp.netty.cmd.CmdHandler;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;

/**
 * Provides a help <verb> system for people to interact with.
 *
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Scott Hernandez
 */
public final class HelpCmd extends BaseCmd {

    public HelpCmd() {
        super("HELP",
                "The HELP command gives help info about the topic specified.\n"
                        + "For a list of topics, type HELP by itself.",
                "[ <topic> ]");
    }

    @Override
    public void execute(String commandString, SmtpSession context) throws IOException {
        String args = this.getArgPredicate(commandString);
        if ("".equals(args)) {
            context.sendResponse(this.getCommandMessage(getSmtpServerConfig()));
            return;
        }
        try {
            context.sendResponse(CmdHandler.getHelp(args).toOutputString());
        } catch (CommandException e) {
            context.sendResponse("504 HELP topic \"" + args + "\" unknown.");
        }
    }

    private String getCommandMessage(SMTPServerConfig config) {
        return "214-"
                + config.getSoftwareName()
                + " on "
                + config.getHostName()
                + "\r\n"
                + "214-Topics:\r\n"
                + this.getFormattedTopicList()
                + "214-For more info use \"HELP <topic>\".\r\n"
                + "214 End of HELP info";
    }

    protected String getFormattedTopicList() {
        StringBuilder sb = new StringBuilder();
        for (String key : CmdHandler.getVerbs()) {
            sb.append("214-     ").append(key).append("\r\n");
        }
        return sb.toString();
    }
}
