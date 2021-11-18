package org.subethamail.smtp.netty.cmd.impl;

import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public final class ResetCmd extends BaseCmd {

	public ResetCmd() {
		super("RSET", "Resets the system.");
	}

	@Override
	public void execute(String commandString, SmtpSession sess) throws IOException {
		sess.resetMailTransaction();

		sess.sendResponse("250 Ok");
	}
}
