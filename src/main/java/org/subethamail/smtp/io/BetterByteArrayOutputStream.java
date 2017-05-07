package org.subethamail.smtp.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Adds a getInputStream() method which does not need to make a copy of the
 * underlying array.
 */
class BetterByteArrayOutputStream extends ByteArrayOutputStream {
    BetterByteArrayOutputStream() {
    }

    BetterByteArrayOutputStream(int size) {
        super(size);
    }

    /**
     * Does not make a copy of the internal buffer.
     */
    InputStream getInputStream() {
        return new ByteArrayInputStream(this.buf, 0, this.count);
    }
}