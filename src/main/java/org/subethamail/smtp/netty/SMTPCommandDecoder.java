package org.subethamail.smtp.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.internal.ObjectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.internal.server.Command;
import org.subethamail.smtp.internal.server.CommandException;
import org.subethamail.smtp.internal.server.CommandHandler;

import java.nio.charset.Charset;
import java.util.List;

public class SMTPCommandDecoder extends StringDecoder {
    private final CommandHandler commandHandler = new CommandHandler();
    // TODO Use CharsetDecoder instead.
    private final Charset charset;
    private final Logger logger = LoggerFactory.getLogger(SMTPCommandDecoder.class);

    /**
     * Creates a new instance with the current system character set.
     */
    public SMTPCommandDecoder() {
        this(Charset.defaultCharset());
    }

    /**
     * Creates a new instance with the specified character set.
     */
    public SMTPCommandDecoder(Charset charset) {
        this.charset = ObjectUtil.checkNotNull(charset, "charset");
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        String commandString = msg.toString(charset);
        logger.info(">> receive:{}", commandString);
        try {
            Command command = CommandHandler.getCommandFromString(commandString);
            out.add(command);
        } catch (CommandException e) {
            ctx.writeAndFlush("500 " + e.getMessage());
        }
    }
}
