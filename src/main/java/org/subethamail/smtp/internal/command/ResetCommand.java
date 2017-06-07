package org.subethamail.smtp.internal.command;

import java.io.IOException;

import org.subethamail.smtp.internal.server.BaseCommand;
import org.subethamail.smtp.server.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public final class ResetCommand extends BaseCommand
{

	public ResetCommand()
	{
		super("RSET", "Resets the system.");
	}

	@Override
	public void execute(String commandString, Session sess) throws IOException
	{
		sess.resetMailTransaction();

		sess.sendResponse("250 Ok");
	}
}
