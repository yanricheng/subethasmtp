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
        SmtpSession session = LocalSessionHolder.get(sessionIdAttr.get());

        String lineFormat = "sessionId:{},text line : -> {}";
        if (msg == null) {
            logger.info(lineFormat, sessionIdAttr.get(), null);
            return;
        }

        byte[] bytes = null;
        int readerIndex;
        if (msg != null) {
            bytes = new byte[msg.readableBytes()];
            readerIndex = msg.readerIndex();
            msg.getBytes(readerIndex, bytes);
            logger.info(lineFormat, sessionIdAttr.get(), new String(bytes), charset);
        }

        String cmdFormat = "sessionId:{},get CMD name: -> {}";
        try {
            String cmdStr = new String(bytes, charset);
            session.setDataFrame(cmdStr);
            Cmd cmd = CmdHandler.getCommandFromString(cmdStr);
            logger.info(cmdFormat, sessionIdAttr.get(), cmd.getName());
            out.add(cmd);
        } catch (UnknownCommandException | InvalidCommandNameException ex) {
            if (session.isDurativeCmd() && session.getLastCmdName() != null) {
                Cmd cmd = commandHandler.getCommand(session.getLastCmdName());
                //取字节数
                if (session.getLastCmdName().equals("BDAT")) {
                    byte[] dataBytes = Arrays.copyOfRange(bytes, 0, session.getHeaderTrimSize());
                    String cmdStr = new String(Arrays.copyOfRange(bytes, session.getHeaderTrimSize(), bytes.length), charset);
                    if(!cmdStr.startsWith("BDAT ")){
                        String message = "503 Error: expected BDAT command line but encountered: '" + cmdStr + "'";
                        session.sendResponse(message);
                    }
                    cmd = BdatCmd.newInstance(cmdStr);
                    session.setDataFrame(cmdStr);
                    if (!session.isDurativeCmd()) {
                        ByteBuf lastBuf = readFixLength(ctx, msg, session.getHeaderTrimSize());
                        if (lastBuf != null) {
                            byte[] lastBytes = new byte[msg.readableBytes()];
                            int lastReaderIndex = msg.readerIndex();
                            lastBuf.getBytes(lastReaderIndex, lastBytes);
                            session.getMail().get().getDataByteOutStream().write(dataBytes);
                            session.sendResponse("250 Message OK, " + session.getHeaderTrimSize() + " bytes received (last chunk)");
                            session.resetMailTransaction();
                        }
                    } else {
                        session.getMail().get().getDataByteOutStream().write(dataBytes);
                        session.sendResponse("250 Message OK, " + session.getHeaderTrimSize() + " bytes received");
                    }
                }

                logger.info(cmdFormat, sessionIdAttr.get(), cmd.getName());
                out.add(cmd);
            } else {
                ctx.writeAndFlush("500 " + ex.getMessage());
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
