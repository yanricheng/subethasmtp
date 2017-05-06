package org.subethamail.smtp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.server.SMTPServer;
import org.subethamail.smtp.server.SMTPServer.Builder;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.Wiser.WiserBuilder;

import junit.framework.TestCase;

/**
 * A base class for testing the SMTP server at the raw protocol level.
 * Handles setting up and tearing down of the server.
 *
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public abstract class ServerTestCase extends TestCase
{
	/** */
	@SuppressWarnings("unused")
	private final static Logger log = LoggerFactory.getLogger(ServerTestCase.class);

	/** */
	public static final int PORT = 2566;

	/** */
	protected Client c;

    private final int maxMessageSize;

    protected Wiser wiser;

	/** */
	public ServerTestCase(String name)
	{
	    this(name, 0);
	}
	
	public ServerTestCase(String name, int maxMessageSize)
    {
        super(name);
        this.maxMessageSize = maxMessageSize;
    }

	/** */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		this.wiser = Wiser.accepter(Testing.ACCEPTER).server(SMTPServer //
		        .port(PORT) //
		        .maxMessageSize(maxMessageSize));
//		this.wiser.setHostname("localhost");
		this.wiser.start();

		this.c = new Client("localhost", PORT);
	}

	/** */
	@Override
	protected void tearDown() throws Exception
	{
		this.wiser.stop();
		this.wiser = null;

		this.c.close();

		super.tearDown();
	}

	/** */
	public void send(String msg) throws Exception
	{
		this.c.send(msg);
	}

	/** */
	public void expect(String msg) throws Exception
	{
		this.c.expect(msg);
	}

	/** */
	public void expectContains(String msg) throws Exception
	{
		this.c.expectContains(msg);
	}
}