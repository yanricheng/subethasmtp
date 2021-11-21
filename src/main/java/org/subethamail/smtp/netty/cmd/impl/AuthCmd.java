package org.subethamail.smtp.netty.cmd.impl;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.netty.auth.AuthHandler;
import org.subethamail.smtp.netty.auth.AuthHandlerFactory;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author Jeff Schnitzer
 * @author Scott Hernandez
 */
public final class AuthCmd extends BaseCmd {

    public static final String VERB = "AUTH";
    public static final String AUTH_CANCEL_COMMAND = "*";

    /**
     * Creates a new instance of AuthCommand
     */
    public AuthCmd() {
        super(
                VERB,
                "Authentication service",
                VERB
                        + " <mechanism> [initial-response] \n"
                        + "\t mechanism = a string identifying a SASL authentication mechanism,\n"
                        + "\t an optional base64-encoded response");
    }

    @Override
    public void execute(String commandString, SmtpSession sess)
            throws IOException {
        if (sess.isAuthenticated()) {
            sess.sendResponse("503 Refusing any other AUTH command.");
            return;
        }

        Optional<AuthHandlerFactory> authFactory = getServerConfig().getAuthHandlerFactory();

        if (!authFactory.isPresent()) {
            sess.sendResponse("502 Authentication not supported");
            return;
        }

        AuthHandler authHandler = authFactory.get().create();

        try {
            if (!sess.getUser().isPresent()) {
                String[] args = getArgs(commandString);
                // Let's check the command syntax
                if (args.length < 2) {
                    sess.sendResponse("501 Syntax: " + VERB + " mechanism [initial-response]");
                    return;
                }
                // Let's check if we support the required authentication mechanism
                String mechanism = args[1];
                if (!authFactory.get().getAuthenticationMechanisms().contains(mechanism.toUpperCase(Locale.ENGLISH))) {
                    sess.sendResponse("504 The requested authentication mechanism is not supported");
                    return;
                }
                // OK, let's go trough the authentication process.
                // The authentication process may require a series of challenge-responses
//			CRLFTerminatedReader reader = sess.getReader();
                Optional<String> response = authHandler.auth(commandString, sess);
                if (response.isPresent()) {
                    // challenge-response iteration
                    sess.sendResponse(response.get());
                    return;
                }
            } else {
                String clientInput = commandString;
                // clientInput == null -> reached EOF, client abruptly closed?
                if (clientInput == null || clientInput.trim().equals(AUTH_CANCEL_COMMAND)) {
                    // RFC 2554 explicitly states this:
                    sess.sendResponse("501 Authentication canceled by client.");
                    return;
                } else {
                    Optional<String> response = authHandler.auth(clientInput, sess);
                    if (response.isPresent()) {
                        // challenge-response iteration
                        sess.sendResponse(response.get());
                        return;
                    }
                }
            }
            sess.sendResponse("235 Authentication successful.");
            sess.setAuthenticated(true);
            sess.setDurativeCmd(false);
        } catch (RejectException authFailed) {
            sess.sendResponse(authFailed.getErrorResponse());
        }
    }
}
