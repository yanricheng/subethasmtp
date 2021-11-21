package org.subethamail.smtp.netty;

import io.netty.channel.ChannelInboundHandlerAdapter;

public class SMTPConstants {
    public static final String SESSION_ID = "sessionId";
    public static final String SMTP_FRAME_DECODER = "smtpFrameDecoder";
    public static final String SMTP_LINE_ENCODER = "lineEncoder";
    public static final String SMTP_CMD_DECODER = "smtpCmdDecoder";
    public static final ChannelInboundHandlerAdapter NOOP_HANDLER = new ChannelInboundHandlerAdapter();
}
