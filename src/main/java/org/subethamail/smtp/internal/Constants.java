package org.subethamail.smtp.internal;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Constants {

    private Constants() {
        // prevent instantiation
    }

    public static final Charset SMTP_CHARSET = StandardCharsets.US_ASCII;

    /**
     *  Maximum total length of a command line as <a 
     *  href="https://tools.ietf.org/html/rfc5321#section-4.5.3.1.4> RFC5321
     *  Sections 4.5.3.1.4 and 4.5.3.1.5</a>
     */
    public static final int SMTP_MAX_LINE_LEN = 512;

}
