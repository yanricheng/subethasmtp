package org.subethamail.smtp.netty.cmd;

import org.subethamail.smtp.DropConnectionException;
import org.subethamail.smtp.internal.server.CommandException;
import org.subethamail.smtp.internal.server.HelpMessage;
import org.subethamail.smtp.netty.SMTPConfig;
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

	void setSmtpConfig(SMTPConfig smtpConfig);

	void execute(String commandString, SmtpSession sess) throws IOException,
			DropConnectionException;

	void execute(SmtpSession sess) throws IOException,
			DropConnectionException;

	HelpMessage getHelp() throws CommandException;

	String getCommandString();

	void setCommandString(String commandString);

	/**
	 * Returns the name of the command in upper case. For example "QUIT".
	 */
	String getName();
}
