package org.subethamail.smtp.server;

import java.io.IOException;
import java.net.ServerSocket;

public interface ServerSocketCreator {
    /**
     * Returns an unbound server socket.
     * 
     * @return server socket
     * @throws IOException
     */
    ServerSocket createServerSocket() throws IOException;
}
