/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp.netty.mail.handler;


import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.netty.session.SmtpSession;

/**
 * The primary interface to be implemented by clients of the SMTP library.
 * This factory is called for every message to be exchanged in an SMTP
 * conversation.  If multiple messages are transmitted in a single connection
 * (via RSET), multiple handlers will be created from this factory.
 *
 * @author Jeff Schnitzer
 */
public interface MsgHandlerFactory
{
	/**
	 * Called for the exchange of a single message during an SMTP conversation.
	 *
	 * @param ctx provides information about the client.
	 */
	MsgHandler create(SmtpSession ctx);
}
