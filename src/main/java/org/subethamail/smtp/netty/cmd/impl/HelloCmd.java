package org.subethamail.smtp.netty.cmd.impl;

import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
public final class HelloCmd extends BaseCmd
{

	public HelloCmd()
	{
		super("HELO", "Introduce yourself.", "<hostname>");
	}

	@Override
	public void execute(String commandString, SmtpSession sess) throws IOException
	{
		String[] args = getArgs(commandString);
		if (args.length < 2)
		{
			sess.sendResponse("501 Syntax: HELO <hostname>");
			return;
		}

//		sess.resetMailTransaction();
		sess.setHelo(args[1]);

		sess.sendResponse("250 " + getSmtpServerConfig().getHostName());
	}
}
