package org.subethamail.smtp.internal.util;

import static org.junit.Assert.assertEquals;
import static org.subethamail.smtp.internal.util.HexUtils.toHex;
import org.junit.Test;

/**
 * Tests for {@link HexUtils}
 *
 * @author diego.salvi
 */
public class HexUtilsTest {

    @Test
    public final void test() {

        assertEquals("FF", toHex(new byte[]{-1}));
        assertEquals("7F", toHex(new byte[]{127}));
        assertEquals("01", toHex(new byte[]{1}));

        assertEquals("FF7F01", toHex(new byte[]{-1, 127, 1}));
        assertEquals("FF:7F:01", toHex(new byte[]{-1, 127, 1}, ':'));

        assertEquals("FF", toHex(new byte[]{-1, 127, 1}, 0, 1));
        assertEquals("7F", toHex(new byte[]{-1, 127, 1}, 1, 1));
        assertEquals("01", toHex(new byte[]{-1, 127, 1}, 2, 1));
        assertEquals("FF7F", toHex(new byte[]{-1, 127, 1}, 0, 2));
        assertEquals("7F01", toHex(new byte[]{-1, 127, 1}, 1, 2));

        assertEquals("FF", toHex(new byte[]{-1, 127, 1}, 0, 1, ':'));
        assertEquals("7F", toHex(new byte[]{-1, 127, 1}, 1, 1, ':'));
        assertEquals("01", toHex(new byte[]{-1, 127, 1}, 2, 1, ':'));
        assertEquals("FF:7F", toHex(new byte[]{-1, 127, 1}, 0, 2, ':'));
        assertEquals("7F:01", toHex(new byte[]{-1, 127, 1}, 1, 2, ':'));

        assertEquals("", toHex(new byte[]{-1, 127, 1}, 0, 0));
        assertEquals("", toHex(new byte[]{-1, 127, 1}, 1, 0));
        assertEquals("", toHex(new byte[]{-1, 127, 1}, 2, 0));

        assertEquals("", toHex(new byte[]{-1, 127, 1}, 0, 0, ':'));
        assertEquals("", toHex(new byte[]{-1, 127, 1}, 1, 0, ':'));
        assertEquals("", toHex(new byte[]{-1, 127, 1}, 2, 0, ':'));
    }
}
