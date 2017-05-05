package org.subethamail.smtp;

import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.github.davidmoten.junit.Asserts;

public class VersionTest {

    @Test
    public void testImplementation() {
        assertNull(Version.getImplementation());
    }

    @Test
    public void testSpecification() {
        assertNull(Version.getSpecification());
    }

    @Test
    public void isUtility() {
        Asserts.assertIsUtilityClass(Version.class);
    }

}
