package org.subethamail.smtp.netty.cmd.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.DropConnectionException;
import org.subethamail.smtp.internal.server.HelpMessage;
import org.subethamail.smtp.netty.ServerConfig;
import org.subethamail.smtp.netty.cmd.Cmd;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
public abstract class BaseCmd implements Cmd {
    @SuppressWarnings("unused")
    private final static Logger log = LoggerFactory.getLogger(BaseCmd.class);

    /**
     * Name of the command, ie HELO
     */
    private final String name;
    /**
     * The help message for this command
     */
    private final HelpMessage helpMsg;

    private String commandString;
    private ServerConfig serverConfig;

    protected BaseCmd(String name, String help) {
        this.name = name;
        this.helpMsg = new HelpMessage(name, help);
    }

    protected BaseCmd(String name, String help, String argumentDescription) {
        this.name = name;
        this.helpMsg = new HelpMessage(name, help, argumentDescription);
    }

    public static String[] getArgs(String commandString) {
        List<String> strings = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(commandString);
        while (stringTokenizer.hasMoreTokens()) {
            strings.add(stringTokenizer.nextToken());
        }

        return strings.toArray(new String[0]);
    }

    @Override
    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    @Override
    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    /**
     * This is the main method that you need to override in order to implement a command.
     */
    @Override
    abstract public void execute(String commandString, SmtpSession context)
            throws IOException, DropConnectionException;

    @Override
    public void execute(SmtpSession sess) throws IOException, DropConnectionException {
        execute(sess.getDataFrame(), sess);
    }

    @Override
    public HelpMessage getHelp() {
        return this.helpMsg;
    }

    @Override
    public String getName() {
        return this.name;
    }

    protected String getArgPredicate(String commandString) {
        if (commandString == null || commandString.length() < 4)
            return "";

        return commandString.substring(4).trim();
    }
}