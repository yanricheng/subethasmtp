package org.subethamail.smtp;

import java.io.ByteArrayInputStream;
import java.net.InetAddress;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.internal.io.ReceivedHeaderStream;

import junit.framework.TestCase;

/**
 * This class tests a bug in ReceivedHeaderStream which
 * has since been fixed.
 *
 * @see <a href="http://www.subethamail.org/se/archive_msg.jsp?msgId=59719">http://www.subethamail.org/se/archive_msg.jsp?msgId=59719</a>
 */
public class ReceivedHeaderStreamTest extends TestCase
{
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(ReceivedHeaderStreamTest.class);

	/** */
	public ReceivedHeaderStreamTest(String name)
	{
		super(name);
	}

	/** */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	/** */
	public void testReceivedHeader() throws Exception
	{
		int BUF_SIZE = 10000;
		int offset = 10;
		ByteArrayInputStream in = new ByteArrayInputStream("hello world".getBytes());
		try (ReceivedHeaderStream hdrIS = new ReceivedHeaderStream(in, Optional.of("ehlo"),
				InetAddress.getLocalHost(), "foo", Optional.empty(), "123", Optional.empty())) {
    		byte[] buf = new byte[BUF_SIZE];
    		int len = hdrIS.read(buf, offset, BUF_SIZE-offset);
    
    		String result = new String(buf, offset, len);
    
    		assertTrue(result.endsWith("\nhello world"));
		}
	}
}