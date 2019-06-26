package org.subethamail.smtp.util;

import static org.junit.Assert.assertEquals;

import com.github.davidmoten.junit.Asserts;
import org.junit.Test;
import org.subethamail.smtp.internal.Constants;
import org.subethamail.smtp.internal.util.SMTPResponseHelper;

public class SMTPResponseHelperTest {

    @Test
    public void isUtilityClass() {
        Asserts.assertIsUtilityClass(SMTPResponseHelper.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooShortCode() {
        SMTPResponseHelper.buildResponse("25", "short");
        SMTPResponseHelper.buildResponse(25, "short");
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooLongCode() {
        SMTPResponseHelper.buildResponse("2525", "long");
        SMTPResponseHelper.buildResponse(2525, "long");
    }

    @Test
    public void shortReponse() {
        assertEquals("250 short", SMTPResponseHelper.buildResponse("250", "short"));
    }

    @Test
    public void longReponse() {
        final int usefulLen = Constants.SMTP_MAX_LINE_LEN - 6;
        /* Just one character more than useful line len */
        final int longResponseLen = usefulLen + 1;
        final StringBuilder builder = new StringBuilder(longResponseLen);
        for(int i = 0; i < longResponseLen; ++i) {
            builder.append(i % 10);
        }
        final String longResponse = builder.toString();


        final StringBuilder expectedBuilder = new StringBuilder();
        expectedBuilder.append("250-")
            .append(longResponse.subSequence(0, usefulLen))
            .append("\r\n")
            .append("250 ")
            .append(longResponse.subSequence(usefulLen, longResponse.length()));

        final String expected = expectedBuilder.toString();

        assertEquals(expected, SMTPResponseHelper.buildResponse("250", longResponse));
    }

    @Test
    public void withReturns() {
        assertEquals("250-one\r\n250 two", SMTPResponseHelper.buildResponse("250", "one\r\ntwo"));
        assertEquals("250-one\r\n250 two", SMTPResponseHelper.buildResponse("250", "one\n\rtwo"));
        assertEquals("250-one\r\n250 two", SMTPResponseHelper.buildResponse("250", "one\ntwo"));
        assertEquals("250-one\r\n250 two", SMTPResponseHelper.buildResponse("250", "one\rtwo"));
    }
}
