package org.subethamail.smtp.netty.cmd;

import org.subethamail.smtp.DropConnectionException;
import org.subethamail.smtp.internal.util.EmailUtils;
import org.subethamail.smtp.netty.cmd.impl.BaseCmd;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;
import java.util.Locale;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public final class ReceiptCmd extends BaseCmd {

	public ReceiptCmd() {
		super("RCPT",
				"Specifies the recipient. Can be used any number of times.",
				"TO: <recipient> [ <parameters> ]");
	}

	@Override
	public void execute(String commandString, SmtpSession sess)
			throws IOException, DropConnectionException {
		if (!sess.isMailTransactionInProgress()) {
			sess.sendResponse("503 5.5.1 Error: need MAIL command");
			return;
		} else if (sess.getSmtpConfig().getMaxRecipients() >= 0 &&
				sess.getRecipientCount() >= sess.getSmtpConfig().getMaxRecipients()) {
			sess.sendResponse("452 Error: too many recipients");
			return;
		}

		String args = this.getArgPredicate(commandString);
		if (!args.toUpperCase(Locale.ENGLISH).startsWith("TO:")) {
			sess.sendResponse(
					"501 Syntax: RCPT TO: <address>  Error in parameters: \""
							+ args + "\"");
		} else {
			String recipientAddress = EmailUtils.extractEmailAddress(args, 3);
			sess.getMail().get().getToAddress().add(recipientAddress);
			sess.addRecipient(recipientAddress);
			sess.sendResponse("250 Ok");

//			try
//			{
//				sess.getMail().get().getToAddress().add(recipientAddress);
//				sess.addRecipient(recipientAddress);
//				sess.sendResponse("250 Ok");
//			}
//			catch (DropConnectionException ex)
//			{
//				throw ex; // Propagate this
//			}
//			catch (RejectException ex)
//			{
//				sess.sendResponse(ex.getErrorResponse());
//			}
		}
	}
}
