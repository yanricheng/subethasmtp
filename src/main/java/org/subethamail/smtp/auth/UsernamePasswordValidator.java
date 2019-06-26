package org.subethamail.smtp.auth;

import org.subethamail.smtp.MessageContext;

/**
 * Use this when your authentication scheme uses a username and a password.
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 */
public interface UsernamePasswordValidator
{
	void login(final String username, final String password, MessageContext context) throws LoginFailedException;
}
