package org.subethamail.smtp.netty.mail.ext;

import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.netty.mail.listener.SimpleMsgListener;

import java.io.IOException;
import java.io.InputStream;

public class ConsoleMsgListener implements SimpleMsgListener {
    @Override
    public boolean accept(String from, String recipient) {
        return true;
    }

    @Override
    public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException {

    }
}
