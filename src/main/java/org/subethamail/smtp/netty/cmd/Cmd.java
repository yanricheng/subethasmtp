package org.subethamail.smtp.netty.cmd;

import org.subethamail.smtp.DropConnectionException;
import org.subethamail.smtp.internal.server.CommandException;
import org.subethamail.smtp.internal.server.HelpMessage;
import org.subethamail.smtp.netty.ServerConfig;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;

/**
 * Describes a SMTP command
 *
 * @author Jon Stevens
 * @author Scott Hernandez
 */
public interface Cmd
{

	void setServerConfig(ServerConfig serverConfig);
	ServerConfig getServerConfig();

	void execute(String commandString, SmtpSession sess) throws IOException,
			DropConnectionException;

	void execute(SmtpSession sess) throws IOException,
			DropConnectionException;

	HelpMessage getHelp() throws CommandException;

	/**
	 * Returns the name of the command in upper case. For example "QUIT".
	 */
	String getName();
}
