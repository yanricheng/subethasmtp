package org.subethamail.smtp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.server.SMTPServer;

public class StartTLSFullTest {

    @Test
    public void testStart()
            throws KeyManagementException, NoSuchAlgorithmException, InterruptedException {

        InputStream trustStore = StartTLSFullTest.class.getResourceAsStream("/trustStore.jks");
        final SSLContext sslContext = ExtendedTrustManager
                .createTlsContextWithExtendedTrustManager(trustStore, "password", false);

        // Your message handler factory.
        MessageHandlerFactory mhf = createMessageHandlerFactory();

        SMTPServer server = createTlsSmtpServer(sslContext, mhf);

        server.setHostName("me.com");
        server.setPort(25000);
        // smtpServer.setBindAddress(bindAddress);
        server.setRequireTLS(true);
        server.start();
        Thread.sleep(1000);
        server.stop();
    }

    private SMTPServer createTlsSmtpServer(final SSLContext sslContext, MessageHandlerFactory mhf) {
        return new SMTPServer(mhf) {
            @Override
            public SSLSocket createSSLSocket(Socket socket) throws IOException {
                InetSocketAddress remoteAddress = (InetSocketAddress) socket
                        .getRemoteSocketAddress();

                SSLSocketFactory sf = sslContext.getSocketFactory();
                SSLSocket s = (SSLSocket) (sf.createSocket(socket, remoteAddress.getHostName(),
                        socket.getPort(), true));

                // we are a server
                s.setUseClientMode(false);

                // select protocols and cipher suites
                s.setEnabledProtocols(s.getSupportedProtocols());
                s.setEnabledCipherSuites(s.getSupportedCipherSuites());

                //// Client must authenticate
                // s.setNeedClientAuth(true);

                return s;
            }
        };
    }

    private static MessageHandlerFactory createMessageHandlerFactory() {
        final Logger log = LoggerFactory.getLogger("MyMessageHandlerFactory");
        return new MessageHandlerFactory() {

            @Override
            public MessageHandler create(MessageContext ctx) {
                return new MessageHandler() {

                    @Override
                    public void from(String from) throws RejectException {
                        log.info("from=" + from);
                    }

                    @Override
                    public void recipient(String recipient) throws RejectException {
                        log.info("recipient=" + recipient);
                    }

                    @Override
                    public void data(InputStream data)
                            throws RejectException, TooMuchDataException, IOException {
                        log.info("data");
                    }

                    @Override
                    public void done() {
                        log.info("done");
                    }
                };
            }
        };
    }

}
