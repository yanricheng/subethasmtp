package org.subethamail.smtp;

import static org.junit.Assert.fail;

import java.net.SocketException;

import org.junit.Ignore;
import org.junit.Test;
import org.subethamail.smtp.client.SMTPClient;
import org.subethamail.smtp.client.SMTPClient.Response;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.wiser.Wiser;

/**
 * This class tests connection timeouts.
 * 
 * @author Jeff Schnitzer
 */
public class TimeoutTest {

	public static final int PORT = 2566;

	@Test
	@Ignore
	public void testTimeout() throws Exception {
		Wiser wiser = Wiser.create(SMTPServer.port(PORT).connectionTimeoutMs(1000));
		wiser.start();

		SMTPClient client = SMTPClient.createAndConnect("localhost", PORT);
		client.sendReceive("HELO foo");
		Thread.sleep(2000);
		try {
			Response r = client.sendReceive("HELO bar");
			System.out.println(r.getCode()+ ":" + r.getMessage());
			fail();
		} catch (SocketException e) {
			// expected
		} finally {
			wiser.stop();
		}
	}

}
