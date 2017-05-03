package org.subethamail.smtp.server;

import java.io.InputStream;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.client.SmartClient;
import org.subethamail.smtp.util.TextUtils;

/**
 * This class tests whether the event handler methods defined in MessageHandler
 * are called at the appropriate times and in good order.
 */
public class MessageHandlerTest {

    private static SMTPServer create(MessageHandlerFactory f) {
        SMTPServer server = new SMTPServer(f);
        server.setPort(2566);
        server.start();
        return server;
    }

    @Test
    public void testCompletedMailTransaction() throws Exception {
        MessageHandlerFactory f = Mockito.mock(MessageHandlerFactory.class);
        MessageHandler h = Mockito.mock(MessageHandler.class);
        Mockito.when(f.create(Mockito.any(MessageContext.class))).thenReturn(h);
        SMTPServer server = create(f);
        try {
            SmartClient client = new SmartClient("localhost", server.getPort(), "localhost");
            client.from("john@example.com");
            client.to("jane@example.com");
            client.dataStart();
            client.dataWrite(TextUtils.getAsciiBytes("body"), 4);
            client.dataEnd();
            client.quit();
        } finally {
            server.stop(); // wait for the server to catch up
        }
        InOrder o = Mockito.inOrder(f, h);
        o.verify(f).create(Mockito.any(MessageContext.class));
        o.verify(h).from("john@example.com");
        o.verify(h).recipient("jane@example.com");
        o.verify(h).data(Mockito.any(InputStream.class));
        o.verify(h).done();
        Mockito.verifyNoMoreInteractions(f, h);
    }

    @Test
    public void testDisconnectImmediately() throws Exception {
        MessageHandlerFactory f = Mockito.mock(MessageHandlerFactory.class);
        SMTPServer server = create(f);
        try {
            SmartClient client = new SmartClient("localhost", server.getPort(), "localhost");
            client.quit();
        } finally {
            server.stop();
        }
        Mockito.verifyNoMoreInteractions(f);
    }

    @Test
    public void testAbortedMailTransaction() throws Exception {
        MessageHandlerFactory f = Mockito.mock(MessageHandlerFactory.class);
        MessageHandler h = Mockito.mock(MessageHandler.class);
        Mockito.when(f.create(Mockito.any(MessageContext.class))).thenReturn(h);
        SMTPServer server = create(f);
        try {
            SmartClient client = new SmartClient("localhost", server.getPort(), "localhost");
            client.from("john@example.com");
            client.quit();
        } finally {
            server.stop(); // wait for the server to catch up
        }
        InOrder o = Mockito.inOrder(f, h);
        o.verify(f).create(Mockito.any(MessageContext.class));
        o.verify(h).from("john@example.com");
        o.verify(h).done();
        Mockito.verifyNoMoreInteractions(f, h);
    }

    @Test
    public void testTwoMailsInOneSession() throws Exception {

        // new Expectations() {
        // {
        // messageHandlerFactory.create((MessageContext) any);
        // result = messageHandler;
        //
        // onInstance(messageHandler).from(anyString);
        // onInstance(messageHandler).recipient(anyString);
        // onInstance(messageHandler).data((InputStream) any);
        // onInstance(messageHandler).done();
        //
        // messageHandlerFactory.create((MessageContext) any);
        // result = messageHandler2;
        //
        // onInstance(messageHandler2).from(anyString);
        // onInstance(messageHandler2).recipient(anyString);
        // onInstance(messageHandler2).data((InputStream) any);
        // onInstance(messageHandler2).done();
        // }
        // };

        MessageHandlerFactory f = Mockito.mock(MessageHandlerFactory.class);
        MessageHandler h = Mockito.mock(MessageHandler.class);
        Mockito.when(f.create(Mockito.any(MessageContext.class))).thenReturn(h);
        SMTPServer server = create(f);
        try {
            SmartClient client = new SmartClient("localhost", server.getPort(), "localhost");

            client.from("john1@example.com");
            client.to("jane1@example.com");
            client.dataStart();
            client.dataWrite(TextUtils.getAsciiBytes("body1"), 5);
            client.dataEnd();

            client.from("john2@example.com");
            client.to("jane2@example.com");
            client.dataStart();
            client.dataWrite(TextUtils.getAsciiBytes("body2"), 5);
            client.dataEnd();
            client.quit();
        } finally {
            server.stop(); // wait for the server to catch up
        }
        InOrder o = Mockito.inOrder(f, h);
        o.verify(f).create(Mockito.any(MessageContext.class));
        o.verify(h).from("john1@example.com");
        o.verify(h).recipient("jane1@example.com");
        o.verify(h).data(Mockito.any(InputStream.class));
        o.verify(h).done();
        o.verify(f).create(Mockito.any(MessageContext.class));
        o.verify(h).from("john2@example.com");
        o.verify(h).recipient("jane2@example.com");
        o.verify(h).data(Mockito.any(InputStream.class));
        o.verify(h).done();
        Mockito.verifyNoMoreInteractions(f, h);
    }

    /**
     * Test for issue 56: rejecting a Mail From causes IllegalStateException in
     * the next Mail From attempt.
     * 
     * @throws RejectException
     * @see <a
     *      href=http://code.google.com/p/subethasmtp/issues/detail?id=56>Issue
     *      56</a>
     */
    // @Test
    // public void testMailFromRejectedFirst() throws IOException,
    // MessagingException, RejectException
    // {
    // new Expectations() {
    // {
    // messageHandlerFactory.create((MessageContext) any);
    // result = messageHandler;
    //
    // onInstance(messageHandler).from(anyString);
    // result = new RejectException("Test MAIL FROM rejection");
    // onInstance(messageHandler).done();
    //
    // messageHandlerFactory.create((MessageContext) any);
    // result = messageHandler2;
    //
    // onInstance(messageHandler2).from(anyString);
    // onInstance(messageHandler2).done();
    // }
    // };
    //
    // SmartClient client = new SmartClient("localhost", smtpServer.getPort(),
    // "localhost");
    //
    // boolean expectedRejectReceived = false;
    // try {
    // client.from("john1@example.com");
    // } catch (SMTPException e) {
    // expectedRejectReceived = true;
    // }
    // Assert.assertTrue(expectedRejectReceived);
    //
    // client.from("john2@example.com");
    // client.quit();
    //
    // smtpServer.stop(); // wait for the server to catch up
    //
    // }

}
