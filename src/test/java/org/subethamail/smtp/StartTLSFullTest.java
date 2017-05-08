package org.subethamail.smtp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.concurrent.Executors;

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
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.smtp.server.SSLSocketCreator;
import org.subethamail.util.ExtendedTrustManager;

import com.sun.mail.util.MailSSLSocketFactory;

public class StartTLSFullTest {

    private static final String LOCALHOST = "127.0.0.1";
    private static final String EMAIL_TO = "me@gmail.com";
    private static final String EMAIL_FROM = "fred@gmail.com";
    private static final String PASSWORD = "password";
    private static final int PORT = 25000;

    @Test
    public void testStartTLS() throws Exception {
        // the server is started using keyStore.jks and trustStore.jks on the
        // classpath
        // the trustStore contains the keyStore certificate (the server trusts
        // itself)
        // the send method uses the same trustStore (and the default keyStore?)
        // to send

        // System.setProperty("javax.net.debug", "all");

        KeyManager[] keyManagers = getKeyManagers();
        TrustManager[] trustManagers = getTrustManagers();

        SSLContext sslContext = createTlsSslContext(keyManagers, trustManagers);

        // mock a MessageHandlerFactory to check for delivery
        MessageHandlerFactory mhf = Mockito.mock(MessageHandlerFactory.class);
        MessageHandler mh = Mockito.mock(MessageHandler.class);
        Mockito.when(mhf.create(Mockito.any(MessageContext.class))).thenReturn(mh);

        SSLSocketCreator tlsSocketCreator = createTLSSocketCreator(sslContext);
        SMTPServer server = SMTPServer //
                .port(PORT) //
                .requireTLS() //
                .enableTLS() //
                .messageHandlerFactory(mhf) //
                .executorService(Executors.newSingleThreadExecutor()) //
                .sslSocketCreator(tlsSocketCreator) //
                .build();
        try {
            server.start();
            Thread.sleep(1000);
            send(trustManagers);
        } finally {
            server.stop();
        }
        InOrder o = Mockito.inOrder(mhf, mh);
        o.verify(mhf).create(Mockito.any(MessageContext.class));
        o.verify(mh).from(EMAIL_FROM);
        o.verify(mh).recipient(EMAIL_TO);
        o.verify(mh).data(Mockito.any(InputStream.class));
        o.verify(mh).done();
        o.verifyNoMoreInteractions();
    }

    private SSLContext createTlsSslContext(KeyManager[] keyManagers, TrustManager[] trustManagers)
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, new java.security.SecureRandom());
        return sslContext;
    }

    private TrustManager[] getTrustManagers() {
        InputStream trustStore = StartTLSFullTest.class.getResourceAsStream("/trustStore.jks");
        TrustManager trustManager = new ExtendedTrustManager(trustStore, PASSWORD.toCharArray(), false);
        TrustManager[] trustManagers = new TrustManager[] { trustManager };
        return trustManagers;
    }

    private KeyManager[] getKeyManagers() throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException, UnrecoverableKeyException {
        InputStream keyStore = StartTLSFullTest.class.getResourceAsStream("/keyStore.jks");
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(keyStore, PASSWORD.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, PASSWORD.toCharArray());
        KeyManager[] keyManagers = kmf.getKeyManagers();
        return keyManagers;
    }

    private static SSLSocketCreator createTLSSocketCreator(final SSLContext sslContext) {
        return new SSLSocketCreator() {
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

    private static void send(TrustManager[] trustManagers) throws Exception {
        String to = EMAIL_TO;
        String from = EMAIL_FROM;
        String host = LOCALHOST;
        Properties props = new Properties();
        props.put("mail.debug", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", PORT + "");
        props.put("mail.smtp.starttls.enable", "true");
        // props.put("mail.transport.protocol", "smtp");
        // props.put("mail.smtp.starttls.required", "true");
        // props.put("mail.smtp.ssl.protocols", "TLSv1.2");

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
        message.setSubject("Testing Subject "  + "\u2191");

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
        messageBodyPart.setFileName("man" +  "\u2191" + ".png");
        multipart.addBodyPart(messageBodyPart);

        // Send the complete message parts
        message.setContent(multipart);

        Transport.send(message);
        System.out.println("Sent message successfully....");
    }

}
