package org.subethamail.smtp.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.internal.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.internal.server.CommandException;
import org.subethamail.smtp.internal.server.InvalidCommandNameException;
import org.subethamail.smtp.internal.server.UnknownCommandException;
import org.subethamail.smtp.netty.cmd.Cmd;
import org.subethamail.smtp.netty.cmd.CmdHandler;
import org.subethamail.smtp.netty.session.SmtpSession;
import org.subethamail.smtp.netty.session.impl.LocalSessionHolder;

import java.nio.charset.Charset;
import java.util.List;

public class SMTPCmdDecoder extends StringDecoder {
    private final CmdHandler commandHandler = new CmdHandler();
    private final Logger logger = LoggerFactory.getLogger(SMTPCmdDecoder.class);
    private final Charset charset;

    /**
     * Creates a new instance with the current system character set.
     */
    public SMTPCmdDecoder() {
        this(Charset.defaultCharset());
    }

    /**
     * Creates a new instance with the specified character set.
     */
    public SMTPCmdDecoder(Charset charset) {
        this.charset = ObjectUtil.checkNotNull(charset, "charset");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf(SMTPConstants.SESSION_ID);
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        String format = "sessionId:{},in ->: {}";
        try {
            String commandString = msg.toString(charset);
            logger.info(format, sessionIdAttr.get(), commandString);
            if (sessionIdAttr.get() != null) {
                SmtpSession smtpSession = LocalSessionHolder.get(sessionIdAttr.get());
                if (smtpSession != null) {
                    smtpSession.setDataFrame(commandString);
                }
            }
            Cmd cmdPrototype = CmdHandler.getCommandFromString(commandString);
            out.add(cmdPrototype);
        } catch (UnknownCommandException | InvalidCommandNameException ex) {
            if (sessionIdAttr.get() != null) {
                SmtpSession smtpSession = LocalSessionHolder.get(sessionIdAttr.get());
                if (smtpSession != null) {
                    if (smtpSession.isDurativeCmd() && smtpSession.getLastCmdName() != null) {
                        Cmd cmd = commandHandler.getCommand(smtpSession.getLastCmdName());
                        out.add(cmd);
                    } else {
                        ctx.writeAndFlush("500 " + ex.getMessage());
                    }
                }
            }
        } catch (CommandException e) {
            ctx.writeAndFlush("500 " + e.getMessage());
        }
    }
}
