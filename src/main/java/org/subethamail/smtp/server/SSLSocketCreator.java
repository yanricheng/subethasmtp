package org.subethamail.smtp.server;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

public interface SSLSocketCreator {
    SSLSocket createSSLSocket(Socket socket) throws IOException;
}
