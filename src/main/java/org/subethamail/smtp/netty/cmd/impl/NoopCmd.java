package org.subethamail.smtp.netty.cmd.impl;

import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public final class NoopCmd extends BaseCmd {

	public NoopCmd() {
		super("NOOP", "The noop command");
	}

	@Override
	public void execute(String commandString, SmtpSession sess) throws IOException {
		sess.sendResponse("250 Ok");
	}
}
