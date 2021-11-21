/*
 * Commands.java Created on November 18, 2006, 12:26 PM To change this template,
 * choose Tools | Template Manager and open the template in the editor.
 */

package org.subethamail.smtp.netty.cmd;

import org.subethamail.smtp.netty.cmd.impl.*;

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
    BDAT(new BdatCmd(), true, true);


    private final Cmd command;

    CmdRegistry(Cmd cmd, boolean checkForStartedTLSWhenRequired, boolean checkForAuthIfRequired) {
        final Cmd c;

        if (checkForStartedTLSWhenRequired) {
            c = new RequireTLSCmdWrapper(cmd, cmd);
        } else {
            c = cmd;
        }

        if (checkForAuthIfRequired) {
            this.command = new RequireAuthCmdWrapper(c, cmd);
        } else {
            this.command = c;
        }
    }

    public Cmd getCommand() {
        return this.command;
    }
}
