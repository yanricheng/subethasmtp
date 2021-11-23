package org.subethamail.smtp.netty.mail.handler;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.netty.mail.listener.BasicMsgListener;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BasicMsgHandlerFactory implements MsgHandlerFactory {

    private final BasicMsgListener listener;
    private final int maxMessageSize;

    public BasicMsgHandlerFactory(BasicMsgListener listener, int maxMessageSize) {
        this.listener = listener;
        this.maxMessageSize = maxMessageSize;
    }

    @Override
    public MsgHandler create(SmtpSession context) {
        return new BasicMsgHandler(context, listener, maxMessageSize);
    }

    public static class BasicMsgHandler implements MsgHandler {

        private final BasicMsgListener listener;
        private final SmtpSession context;
        private final int maxMessageSize;
        private String from;
        private String recipient;


        public BasicMsgHandler(SmtpSession context, BasicMsgListener listener, int maxMessageSize) {
            this.context = context;
            this.listener = listener;
            this.maxMessageSize = maxMessageSize;
        }

        private static byte[] readAndClose(InputStream is, int maxMessageSize)
                throws IOException {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            try {
                while ((n = is.read(buffer)) != -1) {
                    bytes.write(buffer, 0, n);
                    if (maxMessageSize > 0 && bytes.size() > maxMessageSize) {
                        throw new TooMuchDataException("message size exceeded maximum of " + maxMessageSize + "bytes");
                    }
                }
            } finally {
                // TODO creator of stream should close it, not this method
                is.close();
            }
            return bytes.toByteArray();
        }

        @Override
        public void from(String from) throws RejectException {
            this.from = from;

        }

        @Override
        public void recipient(String recipient) throws RejectException {
            this.recipient = recipient;
        }

        @Override
        public String data(InputStream is) throws RejectException, IOException {
            try {
                byte[] bytes = readAndClose(is, maxMessageSize);

                // must call listener here because if called from done() then
                // a 250 ok response has already been sent
                if (from == null) {
                    throw new RejectException("from not set");
                }
                if (recipient == null) {
                    throw new RejectException("recipient not set");
                }
                listener.messageArrived(context, from, recipient, bytes);

                return null;
            } catch (RuntimeException e) {
                throw new RejectException("message could not be accepted: " + e.getMessage());
            }
        }

        @Override
        public void done() {
            // do nothing
        }

    }

}
