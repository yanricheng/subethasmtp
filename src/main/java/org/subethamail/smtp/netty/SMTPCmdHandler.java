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
        if (sessionIdAttr.get() == null) {
            String sessionId = UUID.randomUUID().toString().replaceAll("-", "");
            sessionIdAttr.setIfAbsent(sessionId);
            SmtpSession session = new SmtpSession(sessionId, serverConfig);
            LocalSessionHolder.put(sessionId, session);
        }
    }

    @Override
    // (1)
    public void channelActive(final ChannelHandlerContext ctx) {
        SocketChannel channel = (SocketChannel) ctx.channel();
        channel.writeAndFlush("220 " + serverConfig.getHostName() + " ESMTP " + serverConfig.getSoftwareName());

        System.out.println("IP:" + channel.localAddress().getHostString());
        System.out.println("Port:" + channel.localAddress().getPort());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf(SMTPConstants.SESSION_ID);
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        if (sessionIdAttr.get() != null) {
            SmtpSession session = LocalSessionHolder.get(sessionIdAttr.get());
            Cmd cmd = (Cmd) msg;
            cmd.setServerConfig(serverConfig);
            session.setChannel((SocketChannel) ctx.channel());
            try {
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

                    ctx.channel().pipeline().replace(SMTPConstants.SMTP_FRAME_DECODER,
                            SMTPConstants.SMTP_FRAME_DECODER,
                            new BdatFixedLengthFrameDecoder((int) bdatCmd.getBdat().getSize(), bdatCmd.getBdat().isLast()));
                }
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
            LocalSessionHolder.get(sessionIdAttr.get()).resetMailTransaction();
            logger.info("exception reset session");
        }
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf("sessionId");
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        if (sessionIdAttr.get() != null) {
            LocalSessionHolder.remove(sessionIdAttr.get());
            logger.info("unregistered remove session");
        }
        super.channelUnregistered(ctx);
    }
}
