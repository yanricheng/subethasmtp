package org.subethamail.smtp.netty.auth;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.auth.LoginFailedException;
import org.subethamail.smtp.auth.UsernamePasswordValidator;
import org.subethamail.smtp.internal.util.TextUtils;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.util.*;

/**
 * Implements the SMTP AUTH LOGIN mechanism.<br>
 * You are only required to plug your UsernamePasswordValidator implementation
 * for username and password validation to take effect.
 * <p>
 * LOGIN is an obsolete authentication method which has no formal specification.
 * There is an expired IETF draft for informational purposes. A Microsoft
 * document can also be found, which intends to specify the LOGIN mechanism. The
 * latter is not entirely compatible, neither with the IETF draft nor with RFC
 * 4954 (SMTP Service Extension for Authentication). However this implementation
 * is likely usable with clients following any of the two documents.
 * 
 * @see <a href="http://tools.ietf.org/html/draft-murchison-sasl-login-00">The
 *      LOGIN SASL Mechanism</a>
 * @see <a
 *      href="http://download.microsoft.com/download/5/d/d/5dd33fdf-91f5-496d-9884-0a0b0ee698bb/%5BMS-XLOGIN%5D.pdf">[MS-XLOGIN]</a>
 * 
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author Jeff Schnitzer
 */
public final class LoginAuthHandlerFactory implements AuthHandlerFactory
{
	private static final List<String> MECHANISMS = new ArrayList<>(1);
	static {
		MECHANISMS.add("LOGIN");
	}

	private final UsernameAndPsdValidator helper;

	public LoginAuthHandlerFactory(UsernameAndPsdValidator helper)
	{
		this.helper = helper;
	}

	@Override
    public List<String> getAuthenticationMechanisms()
	{
		return MECHANISMS;
	}

	@Override
    public AuthHandler create()
	{
		return new Handler();
	}

	/**
	 */
	final class Handler implements AuthHandler
	{
//		private String username;

		@Override
		public Optional<String> auth(String clientInput, SmtpSession context) throws RejectException
		{
			StringTokenizer stk = new StringTokenizer(clientInput);
			String token = stk.nextToken();
			if (token.trim().equalsIgnoreCase("AUTH"))
			{
				if (!stk.nextToken().trim().equalsIgnoreCase("LOGIN"))
				{
					// Mechanism mismatch
					throw new RejectException(504, "AUTH mechanism mismatch");
				}

				if (stk.hasMoreTokens())
				{
					// The client submitted an initial response, which should be
					// the username.
					// .Net's built in System.Net.Mail.SmtpClient sends its
					// authentication this way (and this way only).
					byte[] decoded = Base64.getDecoder().decode(stk.nextToken());
					if (decoded == null)
						throw new RejectException(501, /*5.5.4*/
								"Invalid command argument, not a valid Base64 string"); 
					String username = TextUtils.getStringUtf8(decoded);
					context.setUser(Optional.of(new User(username,null)));

					return Optional.of("334 "
							+ Base64.getEncoder().encodeToString(
									TextUtils.getAsciiBytes("Password:")));
				} else {
					context.setUser(Optional.of(new User(null,null)));
					return Optional.of("334 "
							+ Base64.getEncoder().encodeToString(
									TextUtils.getAsciiBytes("Username:")));
				}
			}

			if (context.getUser().get().getUserName() == null)
			{
				byte[] decoded = Base64.getDecoder().decode(clientInput);
				if (decoded == null)
				{
					throw new RejectException(501, /*5.5.4*/
							"Invalid command argument, not a valid Base64 string");
				}

				String username = TextUtils.getStringUtf8(decoded);
				context.getUser().get().setUserName(username);

				return Optional.of("334 "
						+ Base64.getEncoder().encodeToString(
								TextUtils.getAsciiBytes("Password:")));
			}

			byte[] decoded = Base64.getDecoder().decode(clientInput);
			if (decoded == null)
			{
				throw new RejectException(501, /*5.5.4*/
						"Invalid command argument, not a valid Base64 string");
			}
			String password = TextUtils.getStringUtf8(decoded);
			try
			{
				Optional<User> user = context.getUser();
				if(!user.isPresent()){
					throw new LoginFailedException();
				}

				user.get().setPassword(password);
				String username = user.get().getUserName();
				LoginAuthHandlerFactory.this.helper.login(username, password, context);
			}
			catch (LoginFailedException lfe)
			{
				throw new RejectException(535, /*5.7.8*/
						"Authentication credentials invalid");
			}

			return Optional.empty();
		}

		/* */
		@Override
        public Object getIdentity()
		{
			return null;
		}
	}
}
