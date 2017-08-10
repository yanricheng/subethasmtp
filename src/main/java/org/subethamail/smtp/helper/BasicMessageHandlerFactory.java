package org.subethamail.smtp.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

public class BasicMessageHandlerFactory implements MessageHandlerFactory {

    private final BasicMessageListener listener;

    public BasicMessageHandlerFactory(BasicMessageListener listener) {
        this.listener = listener;
    }

    @Override
    public MessageHandler create(MessageContext ctx) {
        return new BasicMessageHandler(listener);
    }

    public static class BasicMessageHandler implements MessageHandler {

        private final BasicMessageListener listener;

        private String from;
        private String recipient;

        public BasicMessageHandler(BasicMessageListener listener) {
            this.listener = listener;
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
        public void data(InputStream is) throws RejectException, TooMuchDataException, IOException {
            try {
                byte[] bytes = readAndClose(is);

                // must call listener here because if called from done() then
                // a 250 ok response has already been sent
                if (from == null) {
                    throw new RejectException("from not set");
                }
                if (recipient == null) {
                    throw new RejectException("recipient not set");
                }
                listener.messageArrived(from, recipient, bytes);
            } catch (RuntimeException e) {
                throw new RejectException("message could not be accepted: " + e.getMessage());
            }
        }

        private static byte[] readAndClose(InputStream is) throws IOException {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            try {
                // TODO honour max message size by throwing a TooMuchDataException when 
                // bytes.length() is too big
                while ((n = is.read(buffer)) != -1) {
                    bytes.write(buffer, 0, n);
                }
            } finally {
                // TODO creator of stream should close it, not this method
                is.close();
            }
            return bytes.toByteArray();
        }

        @Override
        public void done() {
            // do nothing
        }

    }

}
