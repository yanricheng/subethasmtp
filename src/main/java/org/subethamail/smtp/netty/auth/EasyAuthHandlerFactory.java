package org.subethamail.smtp.netty.auth;

import org.subethamail.smtp.auth.LoginAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.MultipleAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.PlainAuthenticationHandlerFactory;
import org.subethamail.smtp.auth.UsernamePasswordValidator;

/**
 * This a convenient class that saves you setting up the factories that we know
 * about; you can always add more afterwards. Currently this factory supports:
 *
 * PLAIN LOGIN
 *
 * @author Jeff Schnitzer
 */
public final class EasyAuthHandlerFactory extends MultipleAuthHandlerFactory
{
	/** Just hold on to this so that the caller can get it later, if necessary */
	private final UsernameAndPsdValidator validator;

	public EasyAuthHandlerFactory(UsernameAndPsdValidator validator)
	{
		this.validator = validator;
		this.addFactory(new PlainAuthHandlerFactory(this.validator));
		this.addFactory(new LoginAuthHandlerFactory(this.validator));
	}

	public UsernameAndPsdValidator getValidator()
	{
		return this.validator;
	}
}
