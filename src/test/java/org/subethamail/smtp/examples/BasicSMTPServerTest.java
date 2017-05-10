package org.subethamail.smtp.examples;

import org.junit.Test;
import org.subethamail.smtp.examples.BasicSMTPServer;
import org.subethamail.smtp.server.SMTPServer;

public class BasicSMTPServerTest {

    @Test
    public void test() throws InterruptedException {
        SMTPServer server = new BasicSMTPServer().start(25000);
        Thread.sleep(1000);
        server.stop();
    }
    
}
