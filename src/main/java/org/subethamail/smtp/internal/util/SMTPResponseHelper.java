package org.subethamail.smtp.internal.util;

import com.github.davidmoten.guavamini.Preconditions;
import org.subethamail.smtp.internal.Constants;

/**
 * @author Diego Salvi
 */
public final class SMTPResponseHelper {

    private SMTPResponseHelper() {
        // prevent instantiation
    }

    /**
     * @return a valid command response checking for allowed maximum line length and handling multiline
     *         response formatting.
     */
    public static String buildResponse(String code, CharSequence response) {

        /* Not a real code check, here we just need to verify that is a 3 digit code */
        Preconditions.checkArgument(code.length() == 3, "Invalid SMTP response code " + code);

        final int len = response.length();

        /*
         * Perform a response size evaluation just to avoid too many array copies.
         *
         * Constants.SMTP_MAX_LINE_LEN - 6 is the useful line len without 3 digit code, ' ' or '-' and
         * trailing \r\n
         */
        final int evaluatedLen = ((len / (Constants.SMTP_MAX_LINE_LEN - 6)) + 1) * Constants.SMTP_MAX_LINE_LEN;
        final StringBuilder result = new StringBuilder(evaluatedLen);

        int lineLen = 4;

        boolean flushLine = false;

        int lastch = -1;

        char[] line = new char[Constants.SMTP_MAX_LINE_LEN];

        /* Setup "continuation" line header */
        line[0] = code.charAt(0);
        line[1] = code.charAt(1);
        line[2] = code.charAt(2);
        line[3] = '-';

        for(int i = 0; i < len; ++i) {

            char ch = response.charAt(i);

            switch(ch) {
                case '\r':

                    if (lastch != '\n') {
                        flushLine = true;
                    }

                    break;

                case '\n':

                    if (lastch != '\r') {
                        flushLine = true;
                    }

                    break;

                default:
                    line[lineLen++] = ch;
            }

            lastch = ch;

            /* Max len minus 2 char (sequence \r\n) */
            if ( lineLen == Constants.SMTP_MAX_LINE_LEN - 2 || flushLine || i == len - 1 ) {

                flushLine = false;

                if ( i != len - 1 ) {

                    /* This isn't the last row */

                    line[lineLen++] = '\r';
                    line[lineLen++] = '\n';
                } else {
                    /* Last row, replace heading 'NNN-' with 'NNN ' */
                    line[3] = ' ';
                }

                result.append(line,0,lineLen);

                lineLen = 4;
            }

        }

        return result.toString();

    }

    /**
     * @return a valid command response checking for allowed maximum line length and handling multiline
     *         response formatting.
     */
    public static String buildResponse(int code, CharSequence response) {

        /* No a real code check, here we just need to verify that is a 3 digit code */
        Preconditions.checkArgument(code > 99 && code < 1000, "Invalid SMTP response code " + code);

        return buildResponse(Integer.toString(code), response);

    }

}
