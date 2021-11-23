package org.subethamail.smtp.netty.cmd.impl;

import com.github.davidmoten.guavamini.Preconditions;
import org.subethamail.smtp.DropConnectionException;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.internal.util.EmailUtils;
import org.subethamail.smtp.netty.mail.Mail;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Scott Hernandez
 * @author Jeff Schnitzer
 */
public final class MailCmd extends BaseCmd {

    private static final Predicate<String> DEFAULT_EMAIL_ADDRESS_VALIDATOR =  //
            emailAddress -> EmailUtils.isValidEmailAddress(emailAddress, true);

    private final Predicate<String> fromAddressValidator;

    public MailCmd() {
        this(DEFAULT_EMAIL_ADDRESS_VALIDATOR);
    }

    /**
     * //     * @param isValidEmailAddress check is MAIL FROM: address is valid
     */
    public MailCmd(Predicate<String> fromAddressValidator) {
        super("MAIL",
                "Specifies the sender.",
                "FROM: <sender> [ <parameters> ]");
        Preconditions.checkNotNull(fromAddressValidator);
        this.fromAddressValidator = fromAddressValidator;
    }

    /* (non-Javadoc)
     * @see org.subethamail.smtp.server.BaseCommand#execute(java.lang.String, org.subethamail.smtp.server.Session)
     */
    @Override
    public void execute(String commandString, SmtpSession sess) throws IOException,
            DropConnectionException {
        try {
            if (sess.isMailTransactionInProgress()) {
                sess.sendResponse("503 5.5.1 Sender already specified.");
                return;
            }

            if (commandString.trim().equals("MAIL FROM:")) {
                sess.sendResponse("501 Syntax: MAIL FROM: <address>");
                return;
            }

            String args = this.getArgPredicate(commandString);
            if (!args.toUpperCase(Locale.ENGLISH).startsWith("FROM:")) {
                sess.sendResponse(
                        "501 Syntax: MAIL FROM: <address>  Error in parameters: \"" +
                                this.getArgPredicate(commandString) + "\"");
                return;
            }

            String emailAddress = EmailUtils.extractEmailAddress(args, 5);
            if (!fromAddressValidator.test(emailAddress)) {
                sess.sendResponse("553 <" + emailAddress + "> Invalid email address.");
                return;
            }

            // extract SIZE argument from MAIL FROM command.
            // disregard unknown parameters. TODO: reject unknown
            // parameters.
            int size = 0;
            String largs = args.toLowerCase(Locale.ENGLISH);
            int sizec = largs.indexOf(" size=");
            if (sizec > -1) {
                // disregard non-numeric values.
                String ssize = largs.substring(sizec + 6).trim();
                if (ssize.length() > 0 && ssize.matches("[0-9]+")) {
                    size = Integer.parseInt(ssize);
                }
            }
            // Reject the message if the size supplied by the client
            // is larger than what we advertised in EHLO answer.
            if (size > sess.getSmtpConfig().getMaxMessageSize()) {
                sess.sendResponse("552 5.3.4 Message size exceeds fixed limit");
                return;
            }

            sess.setMail(Optional.of(new Mail(emailAddress)));
            sess.setDeclaredMessageSize(size);

            sess.startMailTransaction();
            try {
                sess.getMessageHandler().from(emailAddress);
                sess.setMailTransactionInProgress(true);
            } catch (DropConnectionException ex) {
                // roll back the start of the transaction
                sess.resetMailTransaction();
                throw ex; // Propagate this
            } catch (RejectException ex) {
                // roll back the start of the transaction
                sess.resetMailTransaction();
                sess.sendResponse(ex.getErrorResponse());
                return;
            }

            sess.sendResponse("250 Ok");
        } catch (RuntimeException e) {
            sess.sendResponse("503 Error: " + e.getMessage());
            sess.resetMailTransaction();
        }

//		sess.startMailTransaction();
//
//		try
//		{
//			sess.setMail(Optional.of(new Mail(emailAddress)));
//		}
//		catch (DropConnectionException ex)
//		{
//			// roll back the start of the transaction
//			sess.resetMailTransaction();
//			throw ex; // Propagate this
//		}
//		catch (RejectException ex)
//		{
//			// roll back the start of the transaction
//			sess.resetMailTransaction();
//			sess.sendResponse(ex.getErrorResponse());
//			return;
//		}


    }

}
