package org.subethamail.smtp.internal.server;

import org.subethamail.smtp.DropConnectionException;
import org.subethamail.smtp.server.Session;

import java.io.IOException;

/**
 * Describes a SMTP command
 *
 * @author Jon Stevens
 * @author Scott Hernandez
 */
public interface Command
{

	void execute(String commandString, Session sess) throws IOException,
			DropConnectionException;

	HelpMessage getHelp() throws CommandException;

	/**
	 * Returns the name of the command in upper case. For example "QUIT".
	 */
	String getName();

	default Session getContext() {
		return null;
	}

	default void setContext(Session session) {

	}

}
