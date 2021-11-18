package org.subethamail.smtp.netty.cmd.impl;

import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;

/**
 * @author Michele Zuccala < zuccala.m@gmail.com >
 */
public final class ExpandCmd extends BaseCmd {

	public ExpandCmd() {
		super("EXPN", "The expn command.");
	}

	@Override
	public void execute(String commandString, SmtpSession sess) throws IOException {
		sess.sendResponse("502 EXPN command is disabled");
	}
}
