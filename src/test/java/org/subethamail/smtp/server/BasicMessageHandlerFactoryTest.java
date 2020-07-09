package org.subethamail.smtp.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

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

import org.junit.Test;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.helper.BasicMessageHandlerFactory;
import org.subethamail.smtp.helper.BasicMessageListener;

public class BasicMessageHandlerFactoryTest {

    private static final int PORT = 25000;

    @Test
    public void test() throws Exception {
        SMTPServer server = SMTPServer //
                .port(PORT) //
                .messageHandler(
                        (context, from, to,
                                data) -> System.out.println("message from " + from + " to " + to  + ", sessionId=" + context.getSessionId()
                                        + ":\n" + new String(data, StandardCharsets.UTF_8)))
                .build();
        try {
            server.start();
            send();
        } finally {
            server.stop();
        }
    }
    
    @Test
    public void testHasIp() throws Exception {
    	AtomicReference<String> ip = new AtomicReference<String>();
        SMTPServer server = SMTPServer //
                .port(PORT) //
                .messageHandler(
                        (context, from, to,
                                data) -> ip.set(((InetSocketAddress) context.getRemoteAddress()).getAddress().getHostAddress()))
                .build();
        try {
            server.start();
            send();
            System.out.println("source = " + ip.get());
            assertEquals("127.0.0.1", ip.get());
        } finally {
            server.stop();
        }
    }
    
    static void send() throws Exception {
        String to = "someone@domain.com";
        String from = "me@here.com";
        String host = "localhost";
        Properties props = new Properties();
        props.put("mail.debug", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", PORT + "");

        Session session = Session.getInstance(props);
        // Create a default MimeMessage object.
        Message message = new MimeMessage(session);

        // Set From: header field of the header.
        message.setFrom(new InternetAddress(from));

        InternetAddress[] toAddresses = InternetAddress.parse(to);

        // Set To: header field of the header.
        message.setRecipients(Message.RecipientType.TO, toAddresses);

        // Set Subject: header field
        message.setSubject("Testing Subject " + "\u2191");

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
        messageBodyPart.setFileName("man" + "\u2191" + ".png");
        multipart.addBodyPart(messageBodyPart);

        // Send the complete message parts
        message.setContent(multipart);

        Transport.send(message);
        System.out.println("Sent message successfully....");
    }
    
    @Test(expected =TooMuchDataException.class)
    public void testWhenTooMuchDataThatExceptionIsThrown() throws RejectException, TooMuchDataException, IOException {
        BasicMessageListener listener = mock(BasicMessageListener.class);
        int maxMessageSize = 5;
        BasicMessageHandlerFactory f = new BasicMessageHandlerFactory(listener, maxMessageSize);
        MessageContext context = mock(MessageContext.class);
        MessageHandler mh = f.create(context);
        mh.from("fred@thing.com");
        mh.recipient("anne@place.com");
        mh.data(new ByteArrayInputStream("abcdef".getBytes()));
    }
    
    @Test
    public void testWhenNotTooMuchData() throws RejectException, TooMuchDataException, IOException {
        BasicMessageListener listener = mock(BasicMessageListener.class);
        int maxMessageSize = 6;
        BasicMessageHandlerFactory f = new BasicMessageHandlerFactory(listener, maxMessageSize);
        MessageContext context = mock(MessageContext.class);
        MessageHandler mh = f.create(context);
        mh.from("fred@thing.com");
        mh.recipient("anne@place.com");
        mh.data(new ByteArrayInputStream("abcdef".getBytes()));
    }

}
