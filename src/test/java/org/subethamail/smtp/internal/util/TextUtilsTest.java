package org.subethamail.smtp.internal.util;

import org.junit.Test;
import org.subethamail.smtp.internal.util.TextUtils;

public class TextUtilsTest {
    
    @Test(expected=IllegalArgumentException.class)
    public void testGetBytesUnsupportedCharset() {
        TextUtils.getBytes("hello there", "DOES NOT EXIST");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testGetStringUnsupportedCharset() {
        TextUtils.getString("hello there".getBytes(), "DOES NOT EXIST");
    }

}
