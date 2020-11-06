package org.subethamail.smtp.server;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.mockito.Mockito;
import org.subethamail.smtp.internal.proxy.ProxyHandler;
import org.subethamail.smtp.internal.server.ServerThread;

public class SessionHandlerTest {

    @Test
    public void testAcceptAll() throws IOException {
        SessionHandler h = SessionHandler.acceptAll();
        SMTPServer server = SMTPServer.port(2020).build();
        try (ServerSocket ss = new ServerSocket(0)) {
            ServerThread serverThread = new ServerThread(server, ss, ProxyHandler.NOP);
            Socket socket = Mockito.mock(Socket.class);
            ByteArrayInputStream in = new ByteArrayInputStream(
                    "hi there".getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Mockito.when(socket.getInputStream()).thenReturn(in);
            Mockito.when(socket.getOutputStream()).thenReturn(out);
            Session session = new Session(server, serverThread, socket, ProxyHandler.NOP);
            assertTrue(h.accept(session).accepted());
        }
    }

}
