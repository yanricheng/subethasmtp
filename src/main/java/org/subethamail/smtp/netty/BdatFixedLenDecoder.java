package org.subethamail.smtp.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ByteProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.DropConnectionException;
import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.internal.util.SMTPResponseHelper;
import org.subethamail.smtp.netty.session.SmtpSession;
import org.subethamail.smtp.netty.session.impl.SessionHolder;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
public class BdatFixedLenDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(BdatFixedLenDecoder.class);
    private final static int BUFFER_SIZE = 1024 * 32; // 32k seems reasonable
    private final int frameLength;
    private final boolean last;
    private final boolean failFast = false;
    private final boolean stripDelimiter = true;
    private final int maxLength = 1024;
    private final boolean takeEffectImmediately;
    /**
     * True if we're discarding input because we're already over maxLength.
     */
    private boolean discarding;
    private int discardedBytes;
    /**
     * Last scan position.
     */
    private int offset;

    /**
     * Creates a new instance.
     *
     * @param frameLength the length of the frame
     */
    public BdatFixedLenDecoder(int frameLength, boolean last) {
        checkPositive(frameLength, "frameLength");
        this.frameLength = frameLength;
        this.last = last;
        this.takeEffectImmediately = false;
    }

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ByteBuf decoded = readFixLength(ctx, in);
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf(SMTPConstants.SESSION_ID);
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        byte[] bytes = null;
        if (decoded != null) {
            bytes = new byte[decoded.readableBytes()];
            int readerIndex = decoded.readerIndex();
            decoded.getBytes(readerIndex, bytes);
        }

        logger.info("decode by me in -> {}", new String(bytes));

        if (decoded != null) {
            try {
                if (sessionIdAttr.get() != null) {
                    String format = "sessionId:{},in ->: {}";
                    SmtpSession session = SessionHolder.get(sessionIdAttr.get());
                    if (session != null) {
                        out.add(decoded);

                        bytes = new byte[decoded.readableBytes()];
                        int readerIndex = decoded.readerIndex();
                        decoded.getBytes(readerIndex, bytes);
                        session.getMail().get().getDataByteOutStream().write(bytes);
                        logger.info(format, session.getId(), new String(bytes));
                        if (last) {
                            byte[] dataBytes = session.getMail().get().getDataByteOutStream().toByteArray();
                            InputStream stream = new BufferedInputStream(new ByteArrayInputStream(dataBytes), BUFFER_SIZE);
                            String dataMessage = null;
                            try {
                                dataMessage = session.getMessageHandler().data(stream);
                                // Just in case the handler didn't consume all the data, we might as
                                // well suck it up so it doesn't pollute further exchanges. This
                                // code used to throw an exception, but this seems an arbitrary part
                                // of the contract that we might as well relax.
                                while (stream.read() != -1) {
                                }

                            } catch (DropConnectionException ex) {
                                throw ex; // Propagate this
                            } catch (RejectException ex) {
                                session.sendResponse(ex.getErrorResponse());
                                return;
                            }
                            if (dataMessage != null) {
                                session.sendResponse(SMTPResponseHelper.buildResponse("250", dataMessage));
                            } else {
                                session.sendResponse("250 Message OK, " + frameLength + " bytes received (last chunk)");
                            }

                            logger.info("receive whole mail data:{}", new String(dataBytes));

                            session.resetMailTransaction();
                        } else {
                            session.sendResponse("250 Message OK, " + frameLength + " bytes received");
                        }
                    }
                }
            } finally {
                ctx.channel().pipeline().replace(
                        SMTPConstants.SMTP_FRAME_DECODER,
                        SMTPConstants.SMTP_FRAME_DECODER,
                        new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("\r\n".getBytes(StandardCharsets.UTF_8))));
//                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        AttributeKey<String> sessionIdKey = AttributeKey.valueOf("sessionId");
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);
        if (sessionIdAttr.get() != null) {
            SessionHolder.get(sessionIdAttr.get()).resetMailTransaction();
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
    protected ByteBuf readFixLength(
            @SuppressWarnings("UnusedParameters") ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in.readableBytes() < frameLength) {
            return null;
        } else {
            return in.readRetainedSlice(frameLength);
        }
    }

    /**
     * Create a frame out of the {@link ByteBuf} and return it.
     *
     * @param ctx    the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param buffer the {@link ByteBuf} from which to read data
     * @return frame           the {@link ByteBuf} which represent the frame or {@code null} if no frame could
     * be created.
     */
    protected ByteBuf readUntiNextLine(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        final int eol = findEndOfLine(buffer);
        if (!discarding) {
            if (eol >= 0) {
                final ByteBuf frame;
                final int length = eol - buffer.readerIndex();
                final int delimLength = buffer.getByte(eol) == '\r' ? 2 : 1;

                if (length > maxLength) {
                    buffer.readerIndex(eol + delimLength);
                    fail(ctx, length);
                    return null;
                }

                if (stripDelimiter) {
                    frame = buffer.readRetainedSlice(length);
                    buffer.skipBytes(delimLength);
                } else {
                    frame = buffer.readRetainedSlice(length + delimLength);
                }

                return frame;
            } else {
                final int length = buffer.readableBytes();
                if (length > maxLength) {
                    discardedBytes = length;
                    buffer.readerIndex(buffer.writerIndex());
                    discarding = true;
                    offset = 0;
                    if (failFast) {
                        fail(ctx, "over " + discardedBytes);
                    }
                }
                return null;
            }
        } else {
            if (eol >= 0) {
                final int length = discardedBytes + eol - buffer.readerIndex();
                final int delimLength = buffer.getByte(eol) == '\r' ? 2 : 1;
                buffer.readerIndex(eol + delimLength);
                discardedBytes = 0;
                discarding = false;
                if (!failFast) {
                    fail(ctx, length);
                }
            } else {
                discardedBytes += buffer.readableBytes();
                buffer.readerIndex(buffer.writerIndex());
                // We skip everything in the buffer, we need to set the offset to 0 again.
                offset = 0;
            }
            return null;
        }
    }

    private void fail(final ChannelHandlerContext ctx, int length) {
        fail(ctx, String.valueOf(length));
    }

    private void fail(final ChannelHandlerContext ctx, String length) {
        ctx.fireExceptionCaught(
                new TooLongFrameException(
                        "frame length (" + length + ") exceeds the allowed maximum (" + maxLength + ')'));
    }

    /**
     * Returns the index in the buffer of the end of line found.
     * Returns -1 if no end of line was found in the buffer.
     */
    private int findEndOfLine(final ByteBuf buffer) {
        int totalLength = buffer.readableBytes();
        int i = buffer.forEachByte(buffer.readerIndex() + offset, totalLength - offset, ByteProcessor.FIND_LF);
        if (i >= 0) {
            offset = 0;
            if (i > 0 && buffer.getByte(i - 1) == '\r') {
                i--;
            }
        } else {
            offset = totalLength;
        }
        return i;
    }
}
