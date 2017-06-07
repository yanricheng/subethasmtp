package org.subethamail.smtp.internal.command;

import java.io.IOException;

import org.subethamail.smtp.internal.server.BaseCommand;
import org.subethamail.smtp.server.Session;

/**
 * 
 * @author Michele Zuccala < zuccala.m@gmail.com >
 */
public final class ExpandCommand extends BaseCommand 
{

	public ExpandCommand() 
	{
		super("EXPN", "The expn command.");
	}

	@Override
	public void execute(String commandString, Session sess) throws IOException 
	{
		sess.sendResponse("502 EXPN command is disabled");
	}
}
