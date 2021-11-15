package org.subethamail.smtp.netty.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.DropConnectionException;
import org.subethamail.smtp.internal.server.*;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;
import java.util.*;

/**
 * This class manages execution of a SMTP command.
 *
 * @author Jon Stevens
 * @author Scott Hernandez
 */
public final class CmdHandler {
    private final static Logger log = LoggerFactory.getLogger(CmdHandler.class);

    /**
     * The map of known SMTP commands. Keys are upper case names of the
     * commands.
     */
    private static final Map<String, Cmd> commandMap = new HashMap<>();

    public CmdHandler() {
        // This solution should be more robust than the earlier "manual"
        // configuration.
        for (CmdRegistry registry : CmdRegistry.values()) {
            this.addCommand(registry.getCommand());
        }
    }

    /**
     * Create a command handler with a specific set of commands.
     *
     * @param availableCommands the available commands (not null) TLS note: wrap commands with
     *                          {@link RequireTLSCommandWrapper} when appropriate.
     */
    public CmdHandler(Collection<Cmd> availableCommands) {
        for (Cmd command : availableCommands) {
            this.addCommand(command);
        }
    }

    public static Cmd getCommandFromString(String commandString)
            throws UnknownCommandException, InvalidCommandNameException {
        Cmd command = null;
        String key = toKey(commandString);
        command = commandMap.get(key);
        if (command == null) {
            // some commands have a verb longer than 4 letters
            String verb = toVerb(commandString);
            command = commandMap.get(verb);
        }
        if (command == null) {
            throw new UnknownCommandException("Error: command not implemented");
        }
        return command;
    }

    private static String toKey(String string) throws InvalidCommandNameException {
        if (string == null || string.length() < 4)
            throw new InvalidCommandNameException("Error: bad syntax");

        return string.substring(0, 4).toUpperCase(Locale.ENGLISH);
    }

    private static String toVerb(String string) throws InvalidCommandNameException {
        StringTokenizer stringTokenizer = new StringTokenizer(string);
        if (!stringTokenizer.hasMoreTokens())
            throw new InvalidCommandNameException("Error: bad syntax");

        return stringTokenizer.nextToken().toUpperCase(Locale.ENGLISH);
    }

    public static Set<String> getVerbs() {
        return commandMap.keySet();
    }

    /**
     * @return the HelpMessage object for the given command name (verb)
     * @throws CommandException
     */
    public static HelpMessage getHelp(String command) throws CommandException {
        return getCommandFromString(command).getHelp();
    }

    /**
     * Adds or replaces the specified command.
     */
    public void addCommand(Cmd command) {
        log.debug("Added command: {}", command.getName());

        commandMap.put(command.getName(), command);
    }

    /**
     * Returns the command object corresponding to the specified command name.
     *
     * @param commandName case insensitive name of the command.
     * @return the command object, or null, if the command is unknown.
     */
    public Cmd getCommand(String commandName) {
        String upperCaseCommandName = commandName.toUpperCase(Locale.ENGLISH);
        return commandMap.get(upperCaseCommandName);
    }

    public boolean containsCommand(String command) {
        return commandMap.containsKey(command);
    }

//    public void handleCommand(SmtpSession context, String commandString)
//            throws IOException, DropConnectionException {
//        try {
//            Cmd command = getCommandFromString(commandString);
//            command.execute(commandString, context);
//        } catch (CommandException e) {
//            context.sendResponse("500 " + e.getMessage());
//        }
//    }
}
