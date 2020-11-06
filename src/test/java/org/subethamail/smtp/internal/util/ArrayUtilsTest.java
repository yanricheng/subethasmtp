package org.subethamail.smtp.internal.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Tests for {@link ArrayUtils}
 * @author diego.salvi
 */
public class ArrayUtilsTest {

    @Test
    public void equals() {

        byte[] empty = new byte[0];

        byte[] s1a = new byte[]{1};
        byte[] s1b = new byte[]{2};
        byte[] s2a = new byte[]{1, 2};
        byte[] s3a = new byte[]{-1, 1, 2};
        byte[] s3b = new byte[]{1, 2, -1};

        assertTrue(ArrayUtils.equals(empty, 0, 0, empty, 0, 0));

        assertTrue(ArrayUtils.equals(s1a, 0, 0, s1a, 0, 0));
        assertTrue(ArrayUtils.equals(s1a, 0, 1, s1a, 0, 1));

        try {
            assertFalse(ArrayUtils.equals(empty, 0, 1, s1a, 0, 1));
            assertFalse(ArrayUtils.equals(empty, 0, 1, s1a, 0, 1));
            fail();
        } catch (IllegalArgumentException e) {
        }

        assertTrue(ArrayUtils.equals(s1a, 0, 1, s2a, 0, 1));
        assertTrue(ArrayUtils.equals(s2a, 0, 1, s1a, 0, 1));

        assertFalse(ArrayUtils.equals(s1a, 0, 1, s2a, 1, 2));
        assertFalse(ArrayUtils.equals(s2a, 1, 2, s1a, 0, 1));

        assertFalse(ArrayUtils.equals(s1a, 0, 1, s1b, 0, 1));
        assertFalse(ArrayUtils.equals(s1b, 0, 1, s1a, 0, 1));

        assertTrue(ArrayUtils.equals(s2a, 0, 2, s3a, 1, 3));
        assertTrue(ArrayUtils.equals(s3a, 1, 3, s2a, 0, 2));

        assertTrue(ArrayUtils.equals(s2a, 0, 2, s3b, 0, 2));
        assertTrue(ArrayUtils.equals(s3b, 0, 2, s2a, 0, 2));

    }

}
