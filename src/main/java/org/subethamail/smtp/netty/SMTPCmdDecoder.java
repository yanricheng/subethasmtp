package org.subethamail.smtp.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
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
import org.subethamail.smtp.netty.cmd.RequireAuthCmdWrapper;
import org.subethamail.smtp.netty.cmd.RequireTLSCmdWrapper;
import org.subethamail.smtp.netty.cmd.impl.BdatCmd;
import org.subethamail.smtp.netty.session.SmtpSession;
import org.subethamail.smtp.netty.session.impl.LocalSessionHolder;

import java.nio.charset.Charset;
import java.util.Arrays;
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
        String format = "sessionId:{},cmd name: -> {}";

        if (msg == null) {
            logger.info(format, sessionIdAttr.get(), null);
            return;
        }

        byte[] bytes = null;
        int readerIndex;
        if (msg != null) {
            bytes = new byte[msg.readableBytes()];
            readerIndex = msg.readerIndex();
            msg.getBytes(readerIndex, bytes);
        }

        try {
            String commandString = new String(bytes, charset);
            if (sessionIdAttr.get() != null) {
                SmtpSession smtpSession = LocalSessionHolder.get(sessionIdAttr.get());
                if (smtpSession != null) {
                    smtpSession.setDataFrame(commandString);
                }
            }

            Cmd cmdPrototype = CmdHandler.getCommandFromString(commandString);
            logger.info(format, sessionIdAttr.get(), cmdPrototype.getName());
            out.add(cmdPrototype);
        } catch (UnknownCommandException | InvalidCommandNameException ex) {
            if (sessionIdAttr.get() != null) {
                SmtpSession smtpSession = LocalSessionHolder.get(sessionIdAttr.get());
                if (smtpSession != null) {
                    if (smtpSession.isDurativeCmd() && smtpSession.getLastCmdName() != null) {
                        Cmd cmdPrototype = commandHandler.getCommand(smtpSession.getLastCmdName());

                        //取字节数
                        if (smtpSession.getLastCmdName().equals("BDAT")) {
                            byte[] dataBytes = Arrays.copyOfRange(bytes, 0, smtpSession.getHeaderTrimSize());
                            String commandString = new String(Arrays.copyOfRange(bytes, smtpSession.getHeaderTrimSize(), bytes.length), charset);
//                            cmdPrototype = CmdHandler.getCommandFromString(commandString);
                            cmdPrototype = BdatCmd.newInstance(commandString);
                            smtpSession.setDataFrame(commandString);
                            if (bytes.length == smtpSession.getHeaderTrimSize()) {

                            } else {

                            }

                            BdatCmd bdatCmd = null;
                            if (cmdPrototype instanceof BdatCmd) {
                                bdatCmd = (BdatCmd) cmdPrototype;
                            } else if (cmdPrototype instanceof RequireTLSCmdWrapper) {
                                bdatCmd = (BdatCmd) ((RequireTLSCmdWrapper) cmdPrototype).getOriginCmd();
                            } else if (cmdPrototype instanceof RequireAuthCmdWrapper) {
                                bdatCmd = (BdatCmd) ((RequireAuthCmdWrapper) cmdPrototype).getOriginCmd();
                            }
                            if (!smtpSession.isDurativeCmd()) {
                                ByteBuf lastBuf = readFixLength(ctx, msg, smtpSession.getHeaderTrimSize());
                                if (lastBuf != null) {
                                    byte[] lastBytes = new byte[msg.readableBytes()];
                                    int lastReaderIndex = msg.readerIndex();
                                    lastBuf.getBytes(lastReaderIndex, lastBytes);
                                    smtpSession.getMail().get().getDataByteOutStream().write(dataBytes);
                                    smtpSession.sendResponse("250 Message OK, " + smtpSession.getHeaderTrimSize() + " bytes received (last chunk)");
                                    smtpSession.resetMailTransaction();
                                }
                            } else {
                                smtpSession.getMail().get().getDataByteOutStream().write(dataBytes);
                                smtpSession.sendResponse("250 Message OK, " + smtpSession.getHeaderTrimSize() + " bytes received");
                            }


                        }

                        logger.info(format, sessionIdAttr.get(), cmdPrototype.getName());
                        out.add(cmdPrototype);
                    } else {
                        ctx.writeAndFlush("500 " + ex.getMessage());
                    }
                }
            }
        } catch (CommandException e) {
            ctx.writeAndFlush("500 " + e.getMessage());
        }
    }

    /**
     * Create a frame out of the {@link ByteBuf} and return it.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param in  the {@link ByteBuf} from which to read data
     * @return frame           the {@link ByteBuf} which represent the frame or {@code null} if no frame could
     * be created.
     */
    protected ByteBuf readFixLength(
            @SuppressWarnings("UnusedParameters") ChannelHandlerContext ctx, ByteBuf in, int frameLength) throws Exception {
        if (in.readableBytes() < frameLength) {
            return null;
        } else {
            return in.readRetainedSlice(frameLength);
        }
    }
}
