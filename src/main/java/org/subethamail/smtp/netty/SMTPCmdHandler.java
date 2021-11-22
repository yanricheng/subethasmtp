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
import org.subethamail.smtp.netty.cmd.RequireAuthCmdWrapper;
import org.subethamail.smtp.netty.cmd.RequireTLSCmdWrapper;
import org.subethamail.smtp.netty.cmd.impl.BdatCmd;
import org.subethamail.smtp.netty.session.SmtpSession;
import org.subethamail.smtp.netty.session.impl.LocalSessionHolder;

import java.util.UUID;

@ChannelHandler.Sharable
public class SMTPCmdHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SMTPCmdHandler.class);
    private final ServerConfig serverConfig;

    public SMTPCmdHandler(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf(SMTPConstants.SESSION_ID);
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        SmtpSession session = null;
        if (sessionIdAttr.get() == null) {
            String sessionId = UUID.randomUUID().toString().replaceAll("-", "");
            sessionIdAttr.setIfAbsent(sessionId);
            session = new SmtpSession(sessionId, serverConfig);
            session.setChannel((SocketChannel) ctx.channel());
            LocalSessionHolder.put(sessionId, session);
        }
        logger.info("create connect,register session,id:{}", session.getId());
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf(SMTPConstants.SESSION_ID);
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        logger.info("sessionId:{},begin communicate...", sessionIdAttr.get());
        if (sessionIdAttr.get() != null) {
            SmtpSession session = LocalSessionHolder.get(sessionIdAttr.get());
            session.sendResponse("220 " + serverConfig.getHostName() + " ESMTP " + serverConfig.getSoftwareName());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf(SMTPConstants.SESSION_ID);
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        if (sessionIdAttr.get() != null) {
            SmtpSession session = LocalSessionHolder.get(sessionIdAttr.get());
            if (msg == null) {
                logger.info("sessionId:{},execute cmd:{}", session.getId(), null);
                return;
            }
            Cmd cmd = (Cmd) msg;
            cmd.setServerConfig(serverConfig);
            try {
                logger.info("sessionId:{},executing cmd:{}", session.getId(), cmd.getName());
                cmd.execute(session);
                session.setLastCmdName(cmd.getName());
                if (cmd.getName().equals("BDAT")) {
                    BdatCmd bdatCmd = null;
                    if (cmd instanceof BdatCmd) {
                        bdatCmd = (BdatCmd) cmd;
                    } else if (cmd instanceof RequireTLSCmdWrapper) {
                        bdatCmd = (BdatCmd) ((RequireTLSCmdWrapper) cmd).getOriginCmd();
                    } else if (cmd instanceof RequireAuthCmdWrapper) {
                        bdatCmd = (BdatCmd) ((RequireAuthCmdWrapper) cmd).getOriginCmd();
                    }
                    if (bdatCmd.getBdat().isLast) {
                        ctx.channel().pipeline().replace(SMTPConstants.SMTP_FRAME_DECODER,
                                SMTPConstants.SMTP_FRAME_DECODER,
                                new BdatFixedLenDecoder((int) bdatCmd.getBdat().getSize(), bdatCmd.getBdat().isLast()));
                    }
                }
            } catch (Exception e) {
                logger.error("执行命令异常", e);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        super.exceptionCaught(ctx, cause);
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf("sessionId");
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        if (sessionIdAttr.get() != null) {
            LocalSessionHolder.get(sessionIdAttr.get()).resetMailTransaction();
            logger.info("exception reset session");
        }

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
//        super.channelUnregistered(ctx);
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf("sessionId");
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        if (sessionIdAttr.get() != null) {
            LocalSessionHolder.remove(sessionIdAttr.get());
            logger.info("unregistered remove session");
        }
    }
}
