package org.subethamail.smtp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * This class tests {@link RejectException} behaviours.
 *
 * @author Diego Salvi
 */
public class RejectExceptionTest {

    /** Ensure no stacktraces are generated for RejectException */
    @Test
    public void noStacktrace() {

        checkEmptyExceptionStackTrace(new RejectException());
        checkEmptyExceptionStackTrace(new RejectException("message"));
        checkEmptyExceptionStackTrace(new RejectException(510,"message"));

    }

    /** Ensure right status code returns */
    @Test
    public void code() {

        assertEquals(500, new RejectException(500, "message").getCode());
        assertEquals(RejectException.DEFAULT_CODE, new RejectException("message").getCode());
        assertEquals(RejectException.DEFAULT_CODE, new RejectException().getCode());

    }

    /** Ensure right error response returns */
    @Test
    public void errorResponse() {

        assertEquals("500 message", new RejectException(500, "message").getErrorResponse());
        assertEquals(RejectException.DEFAULT_CODE + " message", new RejectException("message").getErrorResponse());
        assertEquals(RejectException.DEFAULT_CODE + " " + RejectException.DEFAULT_MESSAGE,
                new RejectException().getErrorResponse());

    }

    /** Utility method to check for empty stacktraces */
    private static final void checkEmptyExceptionStackTrace(RejectException e) {

        final StackTraceElement[] stacktrace = e.getStackTrace();

        assertNotNull(stacktrace);
        assertEquals(0,stacktrace.length);
    }

}
