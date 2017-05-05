package org.subethamail.smtp.util;

import org.junit.Assert;
import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class EmailUtilsTest {

    @Test
    public void isUtilityClass() {
        Asserts.assertIsUtilityClass(EmailUtils.class);
    }

    @Test
    public void testSpaceAddressIsNotValid() {
        Assert.assertFalse(EmailUtils.isValidEmailAddress(" "));
    }

    @Test
    public void testBlankAddressIsValid() {
        Assert.assertTrue(EmailUtils.isValidEmailAddress(""));
    }
    

}
