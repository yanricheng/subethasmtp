package org.subethamail.smtp.examples;

import java.io.IOException;
import java.net.UnknownHostException;

import org.junit.Test;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;
import org.subethamail.smtp.examples.BasicSMTPServer;
import org.subethamail.smtp.server.SMTPServer;

public class BasicSMTPServerTest {

    @Test
    public void test() throws InterruptedException, UnknownHostException, SMTPException, IOException {
        SMTPServer server = new BasicSMTPServer().start(25000);
        SmartClient client = SmartClient.createAndConnect("localhost", 25000, "clientHost");
        client.from("me@me.com");
        client.to("fred@gmail.com");
        client.dataStart();
        byte[] bytes = "stuff".getBytes();
        client.dataWrite(bytes, bytes.length);
        client.dataEnd();
        client.quit();
        Thread.sleep(500);
        server.stop();
    }
    
    public static void main(String[] args) {
        BasicSMTPServer.main(args);
    }
    
}
