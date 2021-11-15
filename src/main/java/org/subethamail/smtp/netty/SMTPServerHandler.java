package org.subethamail.smtp.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.netty.cmd.Cmd;
import org.subethamail.smtp.netty.session.SmtpSession;
import org.subethamail.smtp.netty.session.impl.LocalSessionHolder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@ChannelHandler.Sharable
public class SMTPServerHandler extends ChannelInboundHandlerAdapter {

    private final SMTPServerConfig smtpServerConfig;
    Logger logger = LoggerFactory.getLogger(SMTPServerHandler.class);

    public SMTPServerHandler(SMTPServerConfig smtpServerConfig) {
        this.smtpServerConfig = smtpServerConfig;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf("sessionId");
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        if (sessionIdAttr.get() == null) {
            String sessionId = UUID.randomUUID().toString().replaceAll("-", "");
            sessionIdAttr.setIfAbsent(sessionId);
            SmtpSession session = new SmtpSession(smtpServerConfig);
            LocalSessionHolder.put(sessionId, session);
        }
    }

    @Override
    // (1)
    public void channelActive(final ChannelHandlerContext ctx) {
        SocketChannel channel = (SocketChannel) ctx.channel();
        channel.writeAndFlush("220 " + smtpServerConfig.getHostName() + " ESMTP " + smtpServerConfig.getSoftwareName());

        System.out.println("IP:" + channel.localAddress().getHostString());
        System.out.println("Port:" + channel.localAddress().getPort());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf("sessionId");
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        String format = "sessionId:%s,time:%s,msg:%s";
        System.out.println(String.format(format, sessionIdAttr.get(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), msg));
        if (sessionIdAttr.get() != null) {
            SmtpSession session = LocalSessionHolder.get(sessionIdAttr.get());
            Cmd cmd = (Cmd) msg;
            cmd.setSmtpServerConfig(smtpServerConfig);
            session.setChannel((SocketChannel)ctx.channel());
            try {
                cmd.execute(session);
                session.setLastCmdName(cmd.getName());
            } catch (Exception e) {
                logger.error("执行命令异常", e);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf("sessionId");
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        if (sessionIdAttr.get() != null) {
            LocalSessionHolder.remove(sessionIdAttr.get());
            logger.info("remove session");
        }
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf("sessionId");
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        if (sessionIdAttr.get() != null) {
            LocalSessionHolder.remove(sessionIdAttr.get());
            logger.info("remove session");
        }
        super.channelUnregistered(ctx);
    }
}
