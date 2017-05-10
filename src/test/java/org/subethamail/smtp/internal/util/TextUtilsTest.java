package org.subethamail.smtp.internal.util;

import org.junit.Test;
import org.subethamail.smtp.internal.util.TextUtils;

import com.github.davidmoten.junit.Asserts;

public class TextUtilsTest {

    @Test
    public void isUtilityClass() {
        Asserts.assertIsUtilityClass(TextUtils.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetBytesUnsupportedCharset() {
        TextUtils.getBytes("hello there", "DOES NOT EXIST");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStringUnsupportedCharset() {
        TextUtils.getString("hello there".getBytes(), "DOES NOT EXIST");
    }

}
