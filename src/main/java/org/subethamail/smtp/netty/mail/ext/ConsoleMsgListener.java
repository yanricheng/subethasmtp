package org.subethamail.smtp.netty.mail.ext;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.smtp.netty.mail.listener.SimpleMsgListener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ConsoleMsgListener implements SimpleMsgListener {
    Logger logger = LoggerFactory.getLogger(ConsoleMsgListener.class);
    @Override
    public boolean accept(String from, String recipient) {
        logger.info("accept({},{})", from, recipient);
        return true;
    }

    @Override
    public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException, IOException {
        logger.info("deliver({},{},{})", from, recipient, IOUtils.toString(data, Charset.forName("UTF-8")));
    }
}
