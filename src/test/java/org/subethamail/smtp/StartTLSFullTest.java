package org.subethamail.smtp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;
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
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.server.SMTPServer;

import com.sun.mail.util.MailSSLSocketFactory;

public class StartTLSFullTest {

    private static final String PASSWORD = "password";
    private static final int PORT = 25000;

    @Test
    public void testStart() throws Exception {
//        System.setProperty("javax.net.debug", "all");
        System.setProperty("javax.net.ssl.keyStore",
                new File("src/test/resources/keyStore.jks").getAbsolutePath());
        System.setProperty("javax.net.ssl.keyStorePassword", PASSWORD);
        InputStream trustStore = StartTLSFullTest.class.getResourceAsStream("/trustStore.jks");
        InputStream keyStore = StartTLSFullTest.class.getResourceAsStream("/keyStore.jks");
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(keyStore, PASSWORD.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, PASSWORD.toCharArray());
        KeyManager[] keyManagers = kmf.getKeyManagers();
        TrustManager trustManager = new ExtendedTrustManager(trustStore, PASSWORD.toCharArray(),
                false);
        TrustManager[] trustManagers = new TrustManager[] { trustManager };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, new java.security.SecureRandom());

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
            send(trustManagers);
            Thread.sleep(3000);
        } finally {
            server.stop();
        }
    }

    private SMTPServer createTlsSmtpServer(final SSLContext sslContext, MessageHandlerFactory mhf) {
        return new SMTPServer(mhf) {
            @Override
            public SSLSocket createSSLSocket(Socket socket) throws IOException {
                InetSocketAddress remoteAddress = (InetSocketAddress) socket
                        .getRemoteSocketAddress();

                SSLSocketFactory sf = sslContext.getSocketFactory();
                SSLSocket s = (SSLSocket) (sf.createSocket(socket, remoteAddress.getHostName(),
                        socket.getPort(), true));

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
                    public void data(InputStream data)
                            throws RejectException, TooMuchDataException, IOException {
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

    private static void send(TrustManager[] trustManagers) throws Exception {
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
        
        MailSSLSocketFactory sslSocketFactory = new MailSSLSocketFactory("TLSv1.2");
        sslSocketFactory.setTrustManagers(trustManagers);
        props.put("mail.smtp.ssl.socketFactory", sslSocketFactory);
        
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
