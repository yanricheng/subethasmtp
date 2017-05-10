package org.subethamail.smtp.internal;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class Constants {

    private Constants() {
        // prevent instantiation
    }

    public static final Charset SMTP_CHARSET = StandardCharsets.US_ASCII;

}
