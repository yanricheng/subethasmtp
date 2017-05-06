/*
 * $Id$
 * $URL$
 */

package org.subethamail.wiser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import javax.mail.MessagingException;
import javax.mail.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.SimpleMessageListener;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.smtp.server.SMTPServer.Builder;

/**
 * Wiser is a tool for unit testing applications that send mail. Your unit tests
 * can start Wiser, run tests which generate emails, then examine the emails
 * that Wiser received and verify their integrity.
 *
 * Wiser is not intended to be a "real" mail server and is not adequate for that
 * purpose; it simply stores all mail in memory. Use the MessageHandlerFactory
 * interface (optionally with the SimpleMessageListenerAdapter) of SubEthaSMTP
 * instead.
 *
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public class Wiser implements SimpleMessageListener {
    /** */
    private final static Logger log = LoggerFactory.getLogger(Wiser.class);

    /** */
    SMTPServer server;

    /** */
    protected final List<WiserMessage> messages = Collections.synchronizedList(new ArrayList<WiserMessage>());

    public static Wiser port(int port) {
        return create(SMTPServer.port(port));
    }

    public static Wiser create(Builder builder) {
        return new Wiser(builder);
    }

    public static Wiser create() {
        return new Wiser(SMTPServer.port(25).build());
    }

    protected Wiser(SMTPServer server) {
        this.server = server;
    }

    protected Wiser(Builder builder) {
        AtomicReference<Wiser> w = new AtomicReference<Wiser>();
        SimpleMessageListener s = new SimpleMessageListener() {

            @Override
            public boolean accept(String from, String recipient) {
                return w.get().accept(from, recipient);
            }

            @Override
            public void deliver(String from, String recipient, InputStream data)
                    throws TooMuchDataException, IOException {
                w.get().deliver(from, recipient, data);
            }
        };
        w.set(this);
        this.server = builder.simpleMessageListener(s).build();
    }

    /** Starts the SMTP Server */
    public void start() {
        this.server.start();
    }

    /** Stops the SMTP Server */
    public void stop() {
        this.server.stop();
    }

    /** A main() for this class. Starts up the server. */
    public static void main(String[] args) throws Exception {
        Wiser wiser = Wiser.create();
        wiser.start();
    }

    /** Always accept everything */
    public boolean accept(String from, String recipient) {
        if (log.isDebugEnabled())
            log.debug("Accepting mail from " + from + " to " + recipient);

        return true;
    }

    /** Cache the messages in memory */
    public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException {
        if (log.isDebugEnabled())
            log.debug("Delivering mail from " + from + " to " + recipient);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        data = new BufferedInputStream(data);

        // read the data from the stream
        int current;
        while ((current = data.read()) >= 0) {
            out.write(current);
        }

        byte[] bytes = out.toByteArray();

        if (log.isDebugEnabled())
            log.debug("Creating message from data with " + bytes.length + " bytes");

        // create a new WiserMessage.
        this.messages.add(new WiserMessage(this, from, recipient, bytes));
    }

    /**
     * Creates the JavaMail Session object for use in WiserMessage
     */
    protected Session getSession() {
        return Session.getDefaultInstance(new Properties());
    }

    /**
     * Returns the list of WiserMessages.
     * <p>
     * The number of mail transactions and the number of mails may be different.
     * If a message is received with multiple recipients in a single mail
     * transaction, then the list will contain more WiserMessage instances, one
     * for each recipient.
     */
    public List<WiserMessage> getMessages() {
        return this.messages;
    }

    /**
     * @return the server implementation
     */
    public SMTPServer getServer() {
        return this.server;
    }

    /**
     * For debugging purposes, dumps a rough outline of the messages to the
     * output stream.
     */
    public void dumpMessages(PrintStream out) throws MessagingException {
        out.println("----- Start printing messages -----");

        for (WiserMessage wmsg : this.getMessages())
            wmsg.dumpMessage(out);

        out.println("----- End printing messages -----");
    }
}
