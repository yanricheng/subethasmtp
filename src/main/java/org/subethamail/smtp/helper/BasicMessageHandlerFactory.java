package org.subethamail.smtp.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;

import com.github.davidmoten.guavamini.Preconditions;

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
        private byte[] bytes;

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
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] bytes = new byte[8192];
            int n;
            while ((n = is.read(bytes)) != -1) {
                out.write(bytes, 0, n);
            }
            is.close();
            this.bytes = out.toByteArray();

            // must call listener here because if called from done() then
            // a 250 ok response has already been sent
            Preconditions.checkNotNull(from, "from not set");
            Preconditions.checkNotNull(recipient, "recipient not set");
            listener.messageArrived(from, recipient, bytes);
        }

        @Override
        public void done() {
            // do nothing
        }

    }

}
