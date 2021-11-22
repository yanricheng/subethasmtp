package org.subethamail.smtp.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ByteProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A decoder that splits the received {@link ByteBuf}s on line endings.
 * <p>
 * Both {@code "\n"} and {@code "\r\n"} are handled.
 * <p>
 * The byte stream is expected to be in UTF-8 character encoding or ASCII. The current implementation
 * uses direct {@code byte} to {@code char} cast and then compares that {@code char} to a few low range
 * ASCII characters like {@code '\n'} or {@code '\r'}. UTF-8 is not using low range [0..0x7F]
 * byte values for multibyte codepoint representations therefore fully supported by this implementation.
 * <p>
 * For a more general delimiter-based decoder, see {@link DelimiterBasedFrameDecoder}.
 */
public class SMTPLineDecoder extends ByteToMessageDecoder {

    /**
     * Maximum length of a frame we're willing to decode.
     */
    private final int maxLength;
    /**
     * Whether or not to throw an exception as soon as we exceed maxLength.
     */
    private final boolean failFast;
    private final boolean stripDelimiter;
    private final Logger logger = LoggerFactory.getLogger(SMTPLineDecoder.class);
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
     * Creates a new decoder.
     *
     * @param maxLength the maximum length of the decoded frame.
     *                  A {@link TooLongFrameException} is thrown if
     *                  the length of the frame exceeds this value.
     */
    public SMTPLineDecoder(final int maxLength) {
        this(maxLength, true, false);
    }


    /**
     * Creates a new decoder.
     *
     * @param maxLength      the maximum length of the decoded frame.
     *                       A {@link TooLongFrameException} is thrown if
     *                       the length of the frame exceeds this value.
     * @param stripDelimiter whether the decoded frame should strip out the
     *                       delimiter or not
     * @param failFast       If <tt>true</tt>, a {@link TooLongFrameException} is
     *                       thrown as soon as the decoder notices the length of the
     *                       frame will exceed <tt>maxFrameLength</tt> regardless of
     *                       whether the entire frame has been read.
     *                       If <tt>false</tt>, a {@link TooLongFrameException} is
     *                       thrown after the entire frame that exceeds
     *                       <tt>maxFrameLength</tt> has been read.
     */
    public SMTPLineDecoder(final int maxLength, final boolean stripDelimiter, final boolean failFast) {
        this.maxLength = maxLength;
        this.failFast = failFast;
        this.stripDelimiter = stripDelimiter;
    }

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        AttributeKey<String> sessionIdKey = AttributeKey.valueOf(SMTPConstants.SESSION_ID);
        Attribute<String> sessionIdAttr = ctx.channel().attr(sessionIdKey);

        ByteBuf decoded = decode(ctx, in);

        byte[] bytes = null;
        if (decoded != null) {
            bytes = new byte[decoded.readableBytes()];
            int readerIndex = decoded.readerIndex();
            decoded.getBytes(readerIndex, bytes);
        }

        String s = (bytes == null ? null : new String(bytes));

        logger.info("sessionId:{},line -> {}", sessionIdAttr.get(), s);

        if (decoded != null) {
            out.add(decoded);
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
    protected ByteBuf decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
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
