/*
 * Commands.java Created on November 18, 2006, 12:26 PM To change this template,
 * choose Tools | Template Manager and open the template in the editor.
 */

package org.subethamail.smtp.netty.cmd;

import org.subethamail.smtp.netty.cmd.impl.AuthCmd;
import org.subethamail.smtp.netty.cmd.impl.DataCmd;
import org.subethamail.smtp.netty.cmd.impl.EhloCmd;
import org.subethamail.smtp.netty.cmd.impl.ExpandCmd;
import org.subethamail.smtp.netty.cmd.impl.HelloCmd;
import org.subethamail.smtp.netty.cmd.impl.HelpCmd;
import org.subethamail.smtp.netty.cmd.impl.MailCmd;
import org.subethamail.smtp.netty.cmd.impl.NoopCmd;
import org.subethamail.smtp.netty.cmd.impl.QuitCmd;
import org.subethamail.smtp.netty.cmd.impl.ReceiptCmd;
import org.subethamail.smtp.netty.cmd.impl.ResetCmd;
import org.subethamail.smtp.netty.cmd.impl.VerifyCmd;

/**
 * Enumerates all the Commands made available in this release.
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
public enum CmdRegistry {
    AUTH(new AuthCmd(), true, false),
    DATA(new DataCmd(), true, true),
    EHLO(new EhloCmd(), false, false),
    HELO(new HelloCmd(), true, false),
    HELP(new HelpCmd(), true, true),
    MAIL(new MailCmd(), true, true),
    NOOP(new NoopCmd(), false, false),
    QUIT(new QuitCmd(), false, false),
    RCPT(new ReceiptCmd(), true, true),
    RSET(new ResetCmd(), true, false),
    //	STARTTLS(new StartTLSCommand(), false, false),
    VRFY(new VerifyCmd(), true, true),
    EXPN(new ExpandCmd(), true, true),
//	BDAT(new BdatCommand(), true, true)
    ;

    private final Cmd command;

    CmdRegistry(Cmd cmd, boolean checkForStartedTLSWhenRequired, boolean checkForAuthIfRequired) {
        final Cmd c;
        if (checkForStartedTLSWhenRequired)
            c = new RequireTLSCmdWrapper(cmd);
        else
            c = cmd;
        if (checkForAuthIfRequired)
            this.command = new RequireAuthCmdWrapper(c);
        else
            this.command = c;
    }

    public Cmd getCommand() {
        return this.command;
    }
}
