package org.subethamail.smtp.internal.proxy;

import com.github.davidmoten.guavamini.Preconditions;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import org.subethamail.smtp.server.Session;

/**
 * Handle proxied connections and their metadata sent <strong>before</strong> actual SMTP protocol. Implementations
 * <strong>must</strong> be threadsafe.
 *
 * @author Diego Salvi
 */
public interface ProxyHandler {

    public ProxyResult handle(InputStream in, OutputStream out, Session session) throws IOException;

    /**
     * No proxy negotiation
     */
    public static final ProxyHandler NOP = (i, o, e) -> ProxyResult.NOP;

    public static final class ProxyResult {

        /**
         * No real proxy data
         */
        public static final ProxyResult NOP = new ProxyResult(null);

        /**
         * Standard proxy negotiation failure
         */
        public static final ProxyResult FAIL = new ProxyResult(503, "Required Proxy negotiation failed");

        private final boolean success;

        // Only used if not success
        private final int errorCode;
        private final String errorMessage;

        private final InetSocketAddress proxiedAddress;

        public ProxyResult(InetSocketAddress proxiedAddress) {
            this.success = true;
            this.errorCode = 0;
            this.errorMessage = null;
            this.proxiedAddress = proxiedAddress;
        }

        public ProxyResult(int errorCode, String errorMessage) {
            Preconditions.checkArgument(errorCode > 399 && errorCode < 600, "Invalid SMTP response code " + errorCode);
            this.success = false;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.proxiedAddress = null;
        }

        public InetSocketAddress getProxiedAddress() {
            return proxiedAddress;
        }

        public boolean isSuccess() {
            return success;
        }

        public boolean isNOP() {
            return proxiedAddress == null;
        }

        /**
         * If proxy negotiation was a success then returns 0 else returns the SMTP response code representing the reason
         * for the negotiation not being accepted.
         *
         * @return error code for a proxy negotiation that was not accepted
         */
        public int errorCode() {
            return errorCode;
        }

        /**
         * If proxy negotiation was accepted then returns null else returns the message representing the reason for the
         * negotiation not being accepted.
         *
         * @return error message for a proxy negotiation that was not accepted
         */
        public String errorMessage() {
            return errorMessage;
        }
    }

}
