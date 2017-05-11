package org.subethamail.smtp.internal.server;

import java.io.IOException;

import org.subethamail.smtp.DropConnectionException;
import org.subethamail.smtp.server.Session;

/**
 * Verifies the presence of a TLS connection if TLS is required.
 * The wrapped command is executed when the test succeeds.
 *
 * @author Erik van Oosten
 */
public final class RequireTLSCommandWrapper implements Command
{

	private final Command wrapped;

	/**
	 * @param wrapped the wrapped command (not null)
	 */
	public RequireTLSCommandWrapper(Command wrapped)
	{
		this.wrapped = wrapped;
	}

	@Override
    public void execute(String commandString, Session sess) 
			throws IOException, DropConnectionException
	{
		if (!sess.getServer().getRequireTLS() || sess.isTLSStarted())
			wrapped.execute(commandString, sess);
		else
			sess.sendResponse("530 Must issue a STARTTLS command first");
	}

	@Override
    public HelpMessage getHelp() throws CommandException
	{
		return wrapped.getHelp();
	}

	@Override
    public String getName()
	{
		return wrapped.getName();
	}
}
