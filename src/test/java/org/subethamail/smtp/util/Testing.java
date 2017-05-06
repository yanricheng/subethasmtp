package org.subethamail.smtp.util;

import org.subethamail.wiser.Wiser.Accepter;

public final class Testing {

    public static final Accepter ACCEPTER = new Accepter() {
        @Override
        public boolean accept(String from, String recipient) {
            if (recipient.equals("failure@subethamail.org")) {
                return false;
            } else if (recipient.equals("success@subethamail.org")) {
                return true;
            }
            return true;
        }
    };
}
