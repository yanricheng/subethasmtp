package org.subethamail.smtp.client;

import java.util.HashMap;
import java.util.Map;

public class PlainAuthenticatorTest {

	private final Map<String, String> extensions = new HashMap<String, String>();

//	@Test
//	public void testSuccess() throws IOException {
//		extensions.put("AUTH", "GSSAPI DIGEST-MD5 PLAIN");
//		PlainAuthenticator authenticator = new PlainAuthenticator(smartClient,
//				"test", "1234");
//
//		new Expectations() {
//			{
//				smartClient.getExtensions();
//				result = extensions;
//
//				// base 64 encoded NULL test NULL 1234
//				smartClient.sendAndCheck("AUTH PLAIN AHRlc3QAMTIzNA==");
//			}
//		};
//
//		authenticator.authenticate();
//	}

}
