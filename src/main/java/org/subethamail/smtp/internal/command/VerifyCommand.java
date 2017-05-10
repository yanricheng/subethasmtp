package org.subethamail.smtp.internal.command;

import java.io.IOException;

import org.subethamail.smtp.internal.server.BaseCommand;
import org.subethamail.smtp.server.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public final class VerifyCommand extends BaseCommand
{
	/** */
	public VerifyCommand()
	{
		super("VRFY", "The vrfy command.");
	}

	/** */
	@Override
	public void execute(String commandString, Session sess) throws IOException
	{
		sess.sendResponse("502 VRFY command is disabled");
	}
}
