package org.subethamail.smtp.server;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SMTPServerTest {

    private static final int ANY_PORT = 1234;

    @Test
    public void testGetServerThreadName_shouldOverrideTheDefaultThreadNameProvider(){
        SMTPServer server = SMTPServer.port(ANY_PORT)
                                      .serverThreadNameProvider( s -> "custom_name")
                                      .build();

        String serverThreadName = server.getServerThreadName();

        assertEquals("custom_name", serverThreadName);
    }

    @Test
    public void testGetServerThreadName_shouldOverrideTheDefaultName(){
        SMTPServer server = SMTPServer.port(ANY_PORT)
                                      .serverThreadName("custom_name")
                                      .build();

        String serverThreadName = server.getServerThreadName();

        assertEquals("custom_name", serverThreadName);
    }

    @Test(expected = NullPointerException.class)
    public void testGetServerThreadName_shouldFailIfNameIsNull(){
        SMTPServer server = SMTPServer.port(ANY_PORT)
                                      .serverThreadName( null )
                                      .build();

        server.getServerThreadName();
    }

    @Test
    public void testGetServerThreadName_shouldUseTheDefaultThreadName(){
        SMTPServer server = SMTPServer.port(ANY_PORT)
                                      .build();

        String serverThreadName = server.getServerThreadName();

        assertEquals("org.subethamail.smtp.internal.server.ServerThread *:1234", serverThreadName);
    }

}