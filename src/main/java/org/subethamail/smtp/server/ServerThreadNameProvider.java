package org.subethamail.smtp.server;

@FunctionalInterface
public interface ServerThreadNameProvider {

    /**
     * Generates a server thread name for given server
     *
     * @param server
     */
    String getThreadName(SMTPServer server);
}
