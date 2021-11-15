package org.subethamail.smtp.netty.auth;

import org.subethamail.smtp.RejectException;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.util.*;

/**
 * This handler combines the behavior of several other authentication handler factories.
 *
 * @author Jeff Schnitzer
 */
public class MultipleAuthHandlerFactory implements AuthHandlerFactory {
    /**
     * Maps the auth type (eg "PLAIN") to a handler. The mechanism name (key) is in upper case.
     */
    final Map<String, AuthHandlerFactory> plugins = new HashMap<>();

    /**
     * A more orderly list of the supported mechanisms. Mechanism names are in upper case.
     */
    List<String> mechanisms = new ArrayList<>();

    public MultipleAuthHandlerFactory() {
        // Starting with an empty list is ok, let the user add them all
    }

    public MultipleAuthHandlerFactory(Collection<AuthHandlerFactory> factories) {
        for (AuthHandlerFactory fact : factories) {
            this.addFactory(fact);
        }
    }

    public void addFactory(AuthHandlerFactory fact) {
        List<String> partialMechanisms = fact.getAuthenticationMechanisms();
        for (String mechanism : partialMechanisms) {
            if (!this.mechanisms.contains(mechanism)) {
                this.mechanisms.add(mechanism);
                this.plugins.put(mechanism, fact);
            }
        }
    }

    @Override
    public List<String> getAuthenticationMechanisms() {
        return this.mechanisms;
    }

    @Override
    public AuthHandler create() {
        return new Handler();
    }

    /**
     *
     */
    final class Handler implements AuthHandler {
        AuthHandler active;
        /* */
        @Override
        public Optional<String> auth(String clientInput, SmtpSession context) throws RejectException {
            if (this.active == null) {
                String method = null;
                if (context.getUser() != null && context.getUser().isPresent()
                        && context.getUser().get().getAuthMechanism() != null) {
                    method = context.getUser().get().getAuthMechanism().name();
                } else {
                    StringTokenizer stk = new StringTokenizer(clientInput);
                    String auth = stk.nextToken();
                    if (!"AUTH".equalsIgnoreCase(auth))
                        throw new IllegalArgumentException("Not an AUTH command: " + clientInput);

                    method = stk.nextToken();
                }
                AuthHandlerFactory fact = MultipleAuthHandlerFactory.this.plugins
                        .get(method.toUpperCase(Locale.ENGLISH));

                if (fact == null)
                    throw new RejectException(504, "Method not supported");

                this.active = fact.create();
            }
            return this.active.auth(clientInput, context);
        }

        /* */
        @Override
        public Object getIdentity() {
            return this.active.getIdentity();
        }
    }
}
