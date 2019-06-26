package org.subethamail.smtp.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.internal.Constants;
import org.subethamail.smtp.server.SMTPServer;

public final class BasicSMTPServer {

    public static final int DEFAULT_PORT = 25000;

    public SMTPServer start(int port) {
        BasicMessageHandlerFactory myFactory = new BasicMessageHandlerFactory();
        SMTPServer smtpServer = SMTPServer.port(port).messageHandlerFactory(myFactory).build();
        System.out.println("Starting Basic SMTP Server on port " + port + "...");
        smtpServer.start();
        return smtpServer;
    }
    
    private static final class BasicMessageHandlerFactory implements MessageHandlerFactory {

        @Override
        public MessageHandler create(MessageContext ctx) {
            return new Handler();
        }

        static final class Handler implements MessageHandler {

            Handler() {
            }

            @Override
            public void from(String from) throws RejectException {
                System.out.println("FROM:" + from);
            }

            @Override
            public void recipient(String recipient) throws RejectException {
                System.out.println("RECIPIENT:" + recipient);
            }

            @Override
            public String data(InputStream data) throws IOException {
                System.out.println("MAIL DATA");
                System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
                System.out.println(this.convertStreamToString(data));
                System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
                return null;
            }

            @Override
            public void done() {
                System.out.println("Finished");
            }

            private String convertStreamToString(InputStream is) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, Constants.SMTP_CHARSET));
                StringBuilder sb = new StringBuilder();

                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return sb.toString();
            }

        }
    }

    public static void main(String[] args) {
        new BasicSMTPServer().start(DEFAULT_PORT);
        System.out.println("Server running!");
    }
}
