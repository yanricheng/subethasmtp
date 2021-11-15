/*
 * Commands.java Created on November 18, 2006, 12:26 PM To change this template,
 * choose Tools | Template Manager and open the template in the editor.
 */

package org.subethamail.smtp.netty.cmd;

import org.subethamail.smtp.internal.command.*;
import org.subethamail.smtp.internal.server.Command;
import org.subethamail.smtp.internal.server.RequireAuthCommandWrapper;
import org.subethamail.smtp.internal.server.RequireTLSCommandWrapper;
import org.subethamail.smtp.netty.cmd.impl.AuthCmd;
import org.subethamail.smtp.netty.cmd.impl.EhloCmd;
import org.subethamail.smtp.netty.cmd.impl.HelloCmd;
import org.subethamail.smtp.netty.cmd.impl.HelpCmd;

/**
 * Enumerates all the Commands made available in this release.
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
public enum CmdRegistry
{
	AUTH(new AuthCmd(), true, false),
//	DATA(new DataCommand(), true, true),
	EHLO(new EhloCmd(), false, false),
	HELO(new HelloCmd(), true, false),
	HELP(new HelpCmd(), true, true);
//	MAIL(new MailCommand(), true, true),
//	NOOP(new NoopCommand(), false, false),
//	QUIT(new QuitCommand(), false, false),
//	RCPT(new ReceiptCommand(), true, true),
//	RSET(new ResetCommand(), true, false),
//	STARTTLS(new StartTLSCommand(), false, false),
//	VRFY(new VerifyCommand(), true, true),
//	EXPN(new ExpandCommand(), true, true),
//	BDAT(new BdatCommand(), true, true);

	private final Cmd command;

	CmdRegistry(Cmd cmd, boolean checkForStartedTLSWhenRequired, boolean checkForAuthIfRequired)
	{
		final Cmd c;
        if (checkForStartedTLSWhenRequired)
			c = new RequireTLSCmdWrapper(cmd);
		else
			c= cmd;
        if (checkForAuthIfRequired)
            this.command = new RequireAuthCmdWrapper(c);
        else 
            this.command = c;
	}

	public Cmd getCommand()
	{
		return this.command;
	}
}
