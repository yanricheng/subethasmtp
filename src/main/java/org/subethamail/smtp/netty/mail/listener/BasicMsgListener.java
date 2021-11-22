package org.subethamail.smtp.netty.mail.listener;

import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.RejectException;

public interface BasicMsgListener {

    /**
     * Process a message that has just arrived. If you throw and you want the caller
     * to be given the SMTP error response then throw a {@link RejectException}.
     * 
     * @param from
     *            source of message
     * @param to
     *            destination of message
     * @param data
     *            message content
     * @throws RejectException
     *             when caller to be given an SMTP error response
     */
    void messageArrived(MessageContext context, String from, String to, byte[] data) throws RejectException;

}
