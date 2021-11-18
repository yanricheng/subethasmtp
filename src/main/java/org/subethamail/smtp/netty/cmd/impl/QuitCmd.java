package org.subethamail.smtp.netty.cmd.impl;

import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public final class QuitCmd extends BaseCmd {

    public QuitCmd() {
        super("QUIT", "Exit the SMTP session.");
    }

    @Override
    public void execute(String commandString, SmtpSession sess) throws IOException {
        sess.sendResponse("221 Bye");
        sess.quit();
    }
}
