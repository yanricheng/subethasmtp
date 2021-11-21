package org.subethamail.smtp.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.netty.session.SmtpSession;
import org.subethamail.smtp.netty.session.impl.LocalSessionHolder;

import java.util.List;

import static io.netty.util.internal.ObjectUtil.checkPositive;

/**
 * A decoder that splits the received {@link ByteBuf}s by the fixed number
 * of bytes. For example, if you received the following four fragmented packets:
 * <pre>
 * +---+----+------+----+
 * | A | BC | DEFG | HI |
 * +---+----+------+----+
 * </pre>
 * A {@link io.netty.handler.codec.FixedLengthFrameDecoder}{@code (3)} will decode them into the
 * following three packets with the fixed length:
 * <pre>
 * +-----+-----+-----+
 * | ABC | DEF | GHI |
 * +-----+-----+-----+
 * </pre>
 */
public class BdatFixedLengthFrameDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(BdatFixedLengthFrameDecoder.class);

    private final int frameLength;
    private final boolean last;
    private boolean takeEffectImmediately;

    /**
     * Creates a new instance.
     *
     * @param frameLength the length of the frame
     */
    public BdatFixedLengthFrameDecoder(int frameLength, boolean last) {
        checkPositive(frameLength, "frameLength");
        this.frameLength = frameLength;
        this.last = last;
        this.takeEffectImmediately = false;
    }

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf(SMTPConstants.SESSION_ID);
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        if (!takeEffectImmediately) {
            SmtpSession session = LocalSessionHolder.get(sessionIdAttr.get());
            logger.info("sessionId:{},not take effect", session.getId());
            takeEffectImmediately = true;
            return;
        }
        ByteBuf decoded = decode(ctx, in);
        if (decoded != null) {
            try {
                if (sessionIdAttr.get() != null) {
                    String format = "sessionId:{},in ->: {}";
                    SmtpSession session = LocalSessionHolder.get(sessionIdAttr.get());
                    if (session != null) {
                        out.add(decoded);

                        byte[] bytes = new byte[decoded.readableBytes()];
                        int readerIndex = decoded.readerIndex();
                        decoded.getBytes(readerIndex, bytes);
                        session.getMail().get().getDataByteOutStream().write(bytes);
                        logger.info(format, session.getId(), new String(bytes));

                        if (last) {
                            session.sendResponse("250 Message OK, " + frameLength + " bytes received (last chunk)");
                            session.resetMailTransaction();
                        } else {
                            session.sendResponse("250 Message OK, " + frameLength + " bytes received");
                        }

                    }
                }
            } finally {
                //数据读取完之后切换按行读取
                ctx.channel().pipeline().replace(SMTPConstants.SMTP_FRAME_DECODER, SMTPConstants.SMTP_FRAME_DECODER,
                        new LineBasedFrameDecoder(1024));
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

    /**
     * Create a frame out of the {@link ByteBuf} and return it.
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param in  the {@link ByteBuf} from which to read data
     * @return frame           the {@link ByteBuf} which represent the frame or {@code null} if no frame could
     * be created.
     */
    protected ByteBuf decode(
            @SuppressWarnings("UnusedParameters") ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in.readableBytes() < frameLength) {
            return null;
        } else {
            return in.readRetainedSlice(frameLength);
        }
    }
}
