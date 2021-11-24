/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp.netty.mail.handler;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.internal.io.DeferredFileOutputStream;
import org.subethamail.smtp.netty.mail.listener.SimpleMsgListener;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * MessageHandlerFactory implementation which adapts to a collection of
 * MessageListeners. This allows us to preserve the old, convenient interface.
 *
 * @author Jeff Schnitzer
 */
public final class SimpleMsgListenerAdapter implements MsgHandlerFactory {
    /**
     * 5 megs by default. The server will buffer incoming messages to disk when
     * they hit this limit in the DATA received.
     */
    private static final int DEFAULT_DATA_DEFERRED_SIZE = 1024 * 1024 * 5;

    private final Collection<SimpleMsgListener> listeners;
    private final int dataDeferredSize;

    /**
     * Initializes this factory with a single listener.
     *
     * Default data deferred size is 5 megs.
     */
    public SimpleMsgListenerAdapter(SimpleMsgListener listener) {
        this(Collections.singleton(listener), DEFAULT_DATA_DEFERRED_SIZE);
    }

    /**
     * Initializes this factory with the listeners.
     *
     * Default data deferred size is 5 megs.
     */
    public SimpleMsgListenerAdapter(Collection<SimpleMsgListener> listeners) {
        this(listeners, DEFAULT_DATA_DEFERRED_SIZE);
    }

    /**
     * Initializes this factory with the listeners.
     *
     * @param dataDeferredSize
     *            The server will buffer incoming messages to disk when they hit
     *            this limit in the DATA received.
     */
    public SimpleMsgListenerAdapter(Collection<SimpleMsgListener> listeners, int dataDeferredSize) {
        this.listeners = listeners;
        this.dataDeferredSize = dataDeferredSize;
    }

    /*
     *
     * @see
     * org.subethamail.smtp.MessageHandlerFactory#create(org.subethamail.smtp.
     * MessageContext)
     */
    @Override
    public MsgHandler create(SmtpSession ctx) {
        return new SimpleHandler(ctx);
    }

    /**
     * Needed by this class to track which listeners need delivery.
     */
    static class SimpleDelivery {
        private final SimpleMsgListener listener;

        SimpleMsgListener getListener() {
            return this.listener;
        }

        private final String recipient;

        String getRecipient() {
            return this.recipient;
        }

        SimpleDelivery(SimpleMsgListener listener, String recipient) {
            this.listener = listener;
            this.recipient = recipient;
        }
    }

    /**
     * Class which implements the actual handler interface.
     */
    class SimpleHandler implements MsgHandler {
        final SmtpSession ctx;
        String from;
        List<SimpleDelivery> deliveries = new ArrayList<>();

        public SimpleHandler(SmtpSession ctx) {
            this.ctx = ctx;
        }

        @Override
        public void from(String from) throws RejectException {
            this.from = from;
        }

        @Override
        public void recipient(String recipient) throws RejectException {
            boolean addedListener = false;

            for (SimpleMsgListener listener : SimpleMsgListenerAdapter.this.listeners) {
                if (listener.accept(this.from, recipient)) {
                    this.deliveries.add(new SimpleDelivery(listener, recipient));
                    addedListener = true;
                }
            }

            if (!addedListener)
                throw new RejectException(553, "<" + recipient + "> address unknown.");
        }

        @Override
        public String data(InputStream data) throws TooMuchDataException, IOException {
            if (this.deliveries.size() == 1) {
                SimpleDelivery delivery = this.deliveries.get(0);
                delivery.getListener().deliver(this.from, delivery.getRecipient(), data);
            } else {

                try (DeferredFileOutputStream dfos = new DeferredFileOutputStream(
                        SimpleMsgListenerAdapter.this.dataDeferredSize)) {
                    int value;
                    while ((value = data.read()) >= 0) {
                        dfos.write(value);
                    }

                    for (SimpleDelivery delivery : this.deliveries) {
                        delivery.getListener().deliver(this.from, delivery.getRecipient(), dfos.getInputStream());
                    }
                }
            }
            return null;
        }

        @Override
        public void done() {
        }
    }

}
