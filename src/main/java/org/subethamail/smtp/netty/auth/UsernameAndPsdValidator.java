package org.subethamail.smtp.netty.auth;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.netty.session.SmtpSession;

/**
 * Use this when your authentication scheme uses a username and a password.
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
public interface UsernameAndPsdValidator
{
	void login(final String username, final String password, SmtpSession context) throws LoginFailedException;

	boolean  add(String userName,String password);
}
