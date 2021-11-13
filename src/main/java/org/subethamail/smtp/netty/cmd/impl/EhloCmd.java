package org.subethamail.smtp.netty.cmd.impl;

import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.internal.command.AuthCommand;
import org.subethamail.smtp.internal.server.BaseCommand;
import org.subethamail.smtp.internal.util.TextUtils;
import org.subethamail.smtp.netty.session.SmtpSession;
import org.subethamail.smtp.server.Session;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class EhloCmd extends BaseCmd {

    public EhloCmd()
    {
        super("EHLO", "Introduce yourself.", "<hostname>");
    }

    @Override
    public void execute(String commandString, SmtpSession sess) throws IOException
    {
        String[] args = getArgs(commandString);
        if (args.length < 2)
        {
            sess.sendResponse("501 Syntax: EHLO hostname");
            return;
        }

        //TODO yrc
//        sess.resetMailTransaction();

        sess.setHelo(args[1]);

//		postfix returns...
//		250-server.host.name
//		250-PIPELINING
//		250-SIZE 10240000
//		250-ETRN
//		250 8BITMIME

        // Once upon a time this code tracked whether or not HELO/EHLO has been seen
        // already and gave an error msg.  However, this is stupid and pointless.
        // Postfix doesn't care, so we won't either.  If you want more, read:
        // http://homepages.tesco.net/J.deBoynePollard/FGA/smtp-avoid-helo.html

        StringBuilder response = new StringBuilder();

        response.append("250-");
        response.append(getSmtpConfig().getHostName());
        response.append("\r\n" + "250-8BITMIME");

        int maxSize = getSmtpConfig().getMaxMessageSize();
        if (maxSize > 0)
        {
            response.append("\r\n" + "250-SIZE ");
            response.append(maxSize);
        }

        // Enabling / Hiding TLS is a server setting
        if (getSmtpConfig().isEnableTLS() && !getSmtpConfig().isHideTLS())
        {
            response.append("\r\n" + "250-STARTTLS");
        }

        // Chunking (BDAT) support
        response.append("\r\n250-CHUNKING");

        // Check to see if we support authentication
        Optional<AuthenticationHandlerFactory> authFact = getSmtpConfig().getAuthenticationHandlerFactory();
        final boolean displayAuth;
        if (getSmtpConfig().isEnableTLS()) {
            displayAuth = authFact.isPresent();
        } else {
            displayAuth = authFact.isPresent() && (!getSmtpConfig().isEnableTLS() || getSmtpConfig().isShowAuthCapabilitiesBeforeSTARTTLS());
        }
        if (displayAuth)
        {
            List<String> supportedMechanisms = authFact.get().getAuthenticationMechanisms();
            if (!supportedMechanisms.isEmpty())
            {
                response.append("\r\n" + "250-" + AuthCommand.VERB + " ");
                response.append(TextUtils.joinTogether(supportedMechanisms, " "));
            }
        }

        response.append("\r\n" + "250 Ok");
        sess.sendResponse(response.toString());
    }
}
