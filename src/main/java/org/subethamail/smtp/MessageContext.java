/*
 * $Id$
 * $URL$
 */
package org.subethamail.smtp;

import org.subethamail.smtp.server.SMTPServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.cert.Certificate;
import java.util.Optional;

/**
 * Interface which provides context to the message handlers.
 *
 * @author Jeff Schnitzer
 */
public interface MessageContext
{
	/**
	 * @return the SMTPServer object.
	 */
	SMTPServer getSMTPServer();

	/**
	 * @return the IP address of the remote server.
	 * 
	 * <p>Note that the returned object is always an instance of {@link InetSocketAddress} so you 
	 * can cast the returned object to that class for more information. In subethasmtp 6.x the 
	 * method return type will be changed to InetSocketAddress. 
	 */
	SocketAddress getRemoteAddress();
	
	/**
	 * Returns the unique id of the session associated with this message. 
	 */
	default String getSessionId() {
		// this added as a default method to 5.x to preserve api compatibility
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @return the handler instance that was used to authenticate.
	 */
	Optional<AuthenticationHandler> getAuthenticationHandler();

	/**
	 * @return the host name or address literal the client supplied in the HELO
	 *         or EHLO command, or empty if neither of these commands were
	 *         received yet. Note that SubEthaSMTP (along with some MTAs, but
	 *         contrary to RFC 5321) accept mail transactions without these
	 *         commands.
	 */
	Optional<String> getHelo();

	/**
	 * Returns the identity of the peer which was established as part of the TLS handshake
	 * as defined by {@link javax.net.ssl.SSLSession#getPeerCertificates()}.
	 * <p/>
	 * In order to get this information, override {@link SMTPServer#createSSLSocket(java.net.Socket)} and call
	 * {@link javax.net.ssl.SSLSocket#setNeedClientAuth(boolean) setNeedClientAuth(true)} on the created socket.
	 *
	 * @return an ordered array of peer certificates, with the peer's own certificate first followed
	 *         by any certificate authorities, or null (?) when no such information is available
	 * @see javax.net.ssl.SSLSession#getPeerCertificates()
	 */
	Certificate[] getTlsPeerCertificates();

	void sendResponse(String response) throws IOException;

}
