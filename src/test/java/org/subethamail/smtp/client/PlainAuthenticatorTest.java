package org.subethamail.smtp.client;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

public class PlainAuthenticatorTest {

    public static void constructPlainLogin() throws IOException {
        String name = "yrc@yanrc.net";
        String psd = "123456";
        ByteArrayOutputStream out = new ByteArrayOutputStream(512);
        out.write(0);
        out.write(name.getBytes(StandardCharsets.UTF_8));
        out.write(0);
        out.write(psd.getBytes(StandardCharsets.UTF_8));
        System.out.println(Base64.getEncoder().encodeToString(out.toByteArray()));
    }

    public static void main(String[] args) throws IOException {
        constructPlainLogin();
    }

    @Test
    public void testSuccess() throws IOException {
        HashMap<String, String> extensions = new HashMap<String, String>();
        extensions.put("AUTH", "GSSAPI DIGEST-MD5 PLAIN");
        SmartClient smartClient = Mockito.mock(SmartClient.class);
        Mockito.when(smartClient.getExtensions()).thenReturn(extensions);
        PlainAuthenticator authenticator = new PlainAuthenticator(smartClient, "test", "1234");
        authenticator.authenticate();
        InOrder o = Mockito.inOrder(smartClient);
        o.verify(smartClient).getExtensions();
        o.verify(smartClient).sendAndCheck("AUTH PLAIN AHRlc3QAMTIzNA==");
    }

    @Test
    public void testSuccess1() {

        String base64 = "AHRlc3QAMTIzNA==";
        System.out.println(new String(Base64.getDecoder().decode(base64)));
        String s = "\u0000";
        System.out.println(new String(Base64.getEncoder().encode(("test" + s + "1234").getBytes())));
    }

}
