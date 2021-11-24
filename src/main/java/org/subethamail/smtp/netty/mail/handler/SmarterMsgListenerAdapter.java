/*
 * $Id: SimpleMessageListenerAdapter.java 320 2009-05-20 09:19:20Z lhoriman $
 * $URL: https://subethasmtp.googlecode.com/svn/trunk/src/org/subethamail/smtp/helper/SimpleMessageListenerAdapter.java $
 */
package org.subethamail.smtp.netty.mail.handler;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.internal.io.DeferredFileOutputStream;
import org.subethamail.smtp.netty.mail.listener.SmarterMsgListener;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * MessageHandlerFactory implementation which adapts to a collection of
 * SmarterMessageListeners. This is actually half-way between the
 * SimpleMessageListener interface and the raw MessageHandler.
 * <p>
 * The key point is that for any message, every accepted recipient will get a
 * separate delivery.
 *
 * @author Jeff Schnitzer
 */
public class SmarterMsgListenerAdapter implements MsgHandlerFactory {
    /**
     * 5 megs by default. The server will buffer incoming messages to disk when
     * they hit this limit in the DATA received.
     */
    private static final int DEFAULT_DATA_DEFERRED_SIZE = 1024 * 1024 * 5;

    private final Collection<SmarterMsgListener> listeners;
    private final int dataDeferredSize;

    /**
     * Initializes this factory with a single listener.
     * <p>
     * Default data deferred size is 5 megs.
     */
    public SmarterMsgListenerAdapter(SmarterMsgListener listener) {
        this(Collections.singleton(listener), DEFAULT_DATA_DEFERRED_SIZE);
    }

    /**
     * Initializes this factory with the listeners.
     * <p>
     * Default data deferred size is 5 megs.
     */
    public SmarterMsgListenerAdapter(Collection<SmarterMsgListener> listeners) {
        this(listeners, DEFAULT_DATA_DEFERRED_SIZE);
    }

    /**
     * Initializes this factory with the listeners.
     *
     * @param dataDeferredSize The server will buffer incoming messages to disk when they hit
     *                         this limit in the DATA received.
     */
    public SmarterMsgListenerAdapter(Collection<SmarterMsgListener> listeners, int dataDeferredSize) {
        this.listeners = listeners;
        this.dataDeferredSize = dataDeferredSize;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.subethamail.smtp.MessageHandlerFactory#create(org.subethamail.smtp.
     * MessageContext)
     */
    @Override
    public MsgHandler create(SmtpSession ctx) {
        return new SmartHandler();
    }

    /**
     * Class which implements the actual handler interface.
     */
    class SmartHandler implements MsgHandler {
        String from;
        List<SmarterMsgListener.Receiver> deliveries = new ArrayList<>();

        SmartHandler() {
        }

        @Override
        public void from(String from) throws RejectException {
            this.from = from;
        }

        @Override
        public void recipient(String recipient) throws RejectException {
            for (SmarterMsgListener listener : SmarterMsgListenerAdapter.this.listeners) {
                SmarterMsgListener.Receiver rec = listener.accept(this.from, recipient);

                if (rec != null) {
                    this.deliveries.add(rec);
                }
            }

            if (this.deliveries.isEmpty()) {
                throw new RejectException(553, "<" + recipient + "> address unknown.");
            }
        }

        @Override
        public String data(InputStream data) throws IOException {
            if (this.deliveries.size() == 1) {
                this.deliveries.get(0).deliver(data);
            } else {
                try (DeferredFileOutputStream dfos = new DeferredFileOutputStream(
                        SmarterMsgListenerAdapter.this.dataDeferredSize)) {
                    int value;
                    while ((value = data.read()) >= 0) {
                        dfos.write(value);
                    }

                    for (SmarterMsgListener.Receiver rec : this.deliveries) {
                        rec.deliver(dfos.getInputStream());
                    }
                }
            }
            return null;
        }

        @Override
        public void done() {
            for (SmarterMsgListener.Receiver rec : this.deliveries) {
                rec.done();
            }
        }
    }
}
