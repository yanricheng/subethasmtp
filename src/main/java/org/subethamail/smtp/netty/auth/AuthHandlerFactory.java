package org.subethamail.smtp.netty.auth;

import java.util.List;

/**
 * The factory interface for creating authentication handlers.
 *
 * @author Marco Trevisan <mrctrevisan@yahoo.it>
 * @author Jeff Schnitzer
 */
public interface AuthHandlerFactory {
    /**
     * If your handler supports RFC 2554 at some degree, then it must return all the supported mechanisms here. <br>
     * The order you use to populate the list will be preserved in the output of the EHLO command. <br>
     *
     * @return the supported authentication mechanisms as List, names are in upper case.
     */
    List<String> getAuthenticationMechanisms();

    /**
     * Create a fresh instance of your handler.
     *
     * @return a new authentication handler
     */
    AuthHandler create();

}
