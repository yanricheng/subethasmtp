package org.subethamail.smtp.command;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.AuthenticationHandlerFactory;
import org.subethamail.smtp.internal.command.EhloCommand;
import org.subethamail.smtp.internal.server.ServerThread;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.smtp.server.Session;

public class EhloCommandTest {

    @Test
    public void testEhloWhenTlsRequiredAuthShouldNotAdvertiseBeforeTlsStarted() throws IOException {
        String output = getOutput(false);
        System.out.println(output);
        assertTrue(output.contains("250-STARTTLS"));
        assertFalse(output.contains("250-AUTH PLAIN"));
    }
    
    @Test
    public void testEhloWhenTlsRequiredAuthShouldBeAdvertiseAfterTlsStarted() throws IOException {
        String output = getOutput(true);
        System.out.println(output);
        assertTrue(output.contains("250-STARTTLS"));
        assertTrue(output.contains("250-AUTH PLAIN"));
    }

    private String getOutput(boolean isTlsStarted) throws IOException {
        EhloCommand ec = new EhloCommand();
        try (ServerSocket ss = new ServerSocket(0)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Socket socket = Mockito.mock(Socket.class);
            Mockito.when(socket.getOutputStream()).thenReturn(out);
            SMTPServer server = SMTPServer //
                    .port(ss.getLocalPort()) //
                    .serverSocketFactory(() -> ss) //
                    .enableTLS() //
                    .requireTLS() //
                    .authenticationHandlerFactory(new AuthenticationHandlerFactory() {

                        @Override
                        public List<String> getAuthenticationMechanisms() {
                            return Collections.singletonList("PLAIN");
                        }

                        @Override
                        public AuthenticationHandler create() {
                            return null;
                        }
                    }) //
                    .build();
            Session session = new Session(server, new ServerThread(server, ss), socket);
            session.setTlsStarted(isTlsStarted);
            ec.execute("EHLO me.com", session);
            String output = new String(out.toByteArray(), StandardCharsets.UTF_8);
            return output;
        }
    }

}
