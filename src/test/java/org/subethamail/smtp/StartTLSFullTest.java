package org.subethamail.smtp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.server.SMTPServer;

public class StartTLSFullTest {

    private static final int PORT = 25000;

    @Test
    public void testStart() throws Exception {
        System.setProperty("javax.net.debug", "all");
        System.setProperty("javax.net.ssl.keyStore", new File("src/test/resources/keys.jks").getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        InputStream trustStore = StartTLSFullTest.class.getResourceAsStream("/trustStore.jks");
        final SSLContext sslContext = ExtendedTrustManager.createTlsContextWithAlwaysHappyExtendedTrustManager();
        // ExtendedTrustManager.createTlsContextWithExtendedTrustManager(trustStore,
        // "password", false);

        // Your message handler factory.
        MessageHandlerFactory mhf = createMessageHandlerFactory();

        SMTPServer server = createTlsSmtpServer(sslContext, mhf);
        server.setHostName("me.com");
        server.setPort(PORT);
        // smtpServer.setBindAddress(bindAddress);
        // server.setRequireTLS(true);
        server.setEnableTLS(true);
        try {
            server.start();
            Thread.sleep(1000);
            send();
            Thread.sleep(3000);
        } finally {
            server.stop();
        }
    }

    private SMTPServer createTlsSmtpServer(final SSLContext sslContext, MessageHandlerFactory mhf) {
        return new SMTPServer(mhf) {
            @Override
            public SSLSocket createSSLSocket(Socket socket) throws IOException {
                InetSocketAddress remoteAddress = (InetSocketAddress) socket.getRemoteSocketAddress();

                SSLSocketFactory sf = sslContext.getSocketFactory();
                SSLSocket s = (SSLSocket) (sf.createSocket(socket, remoteAddress.getHostName(), socket.getPort(),
                        true));

                // we are a server
                s.setUseClientMode(false);

                // select protocols and cipher suites
                s.setEnabledProtocols(s.getSupportedProtocols());
                s.setEnabledCipherSuites(s.getSupportedCipherSuites());

                //// Client must authenticate
                // s.setNeedClientAuth(true);

                return s;
            }
        };
    }

    private static MessageHandlerFactory createMessageHandlerFactory() {
        final Logger log = LoggerFactory.getLogger("MyMessageHandlerFactory");
        return new MessageHandlerFactory() {

            @Override
            public MessageHandler create(MessageContext ctx) {
                return new MessageHandler() {

                    @Override
                    public void from(String from) throws RejectException {
                        log.info("from=" + from);
                    }

                    @Override
                    public void recipient(String recipient) throws RejectException {
                        log.info("recipient=" + recipient);
                    }

                    @Override
                    public void data(InputStream data) throws RejectException, TooMuchDataException, IOException {
                        log.info("data");
                    }

                    @Override
                    public void done() {
                        log.info("done");
                    }
                };
            }
        };
    }

    private static void send() throws Exception {
        String to = "me@gmail.com";
        String from = "fred@gmail.com";
        String host = "127.0.0.1";
        Properties props = new Properties();
        props.put("mail.debug", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", PORT + "");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.starttls.enable", "true");
        // props.put("mail.smtp.from", from);
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        // MailSSLSocketFactory sslSocketFactory = new
        // MailSSLSocketFactory("TLSv1.2");

        Session session = Session.getInstance(props);
        // Create a default MimeMessage object.
        Message message = new MimeMessage(session);

        // Set From: header field of the header.
        message.setFrom(new InternetAddress(from));

        InternetAddress[] toAddresses = InternetAddress.parse(to);

        // Set To: header field of the header.
        message.setRecipients(Message.RecipientType.TO, toAddresses);

        // Set Subject: header field
        message.setSubject("Testing Subject");

        // Create the message part
        BodyPart messageBodyPart = new MimeBodyPart();

        // Now set the actual message
        messageBodyPart.setText("This is message body");

        // Create a multipar message
        Multipart multipart = new MimeMultipart();

        // Set text message part
        multipart.addBodyPart(messageBodyPart);

        // Part two is attachment
        messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(new File("src/test/resources/man.png"));
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName("man.png");
        multipart.addBodyPart(messageBodyPart);

        // Send the complete message parts
        message.setContent(multipart);

        Transport.send(message);
        System.out.println("Sent message successfully....");

    }

}
