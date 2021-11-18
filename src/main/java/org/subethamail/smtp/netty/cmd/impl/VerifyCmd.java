package org.subethamail.smtp.netty.cmd.impl;

import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public final class VerifyCmd extends BaseCmd {

    public VerifyCmd() {
        super("VRFY", "The vrfy command.");
    }

    @Override
    public void execute(String commandString, SmtpSession sess) throws IOException {
        if (sess.getSmtpConfig().isDisableVerify()) {
            sess.sendResponse("502 VRFY command is disabled");
        } else {
//			C: VRFY Crispin
//			S: 250 Mark Crispin <Admin.MRC@foo.com>
            //TODO test exists
            sess.sendResponse("250 xx<xx@" + sess.getSmtpConfig().getDomain() + ">");
        }
    }
}
