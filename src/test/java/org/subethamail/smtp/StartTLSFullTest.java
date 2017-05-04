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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.server.SMTPServer;

public class StartTLSFullTest {

    private void start() throws KeyManagementException, NoSuchAlgorithmException {

        InputStream trustStore = StartTLSFullTest.class.getResourceAsStream("/trustStore.jks");
        final SSLContext sslContext = ExtendedTrustManager
                .createTlsContextWithExtendedTrustManager(trustStore, "changeIt", false);

        // Your message handler factory.
        MessageHandlerFactory mhf = createMessageHandlerFactory();

        SMTPServer smtpServer = new SMTPServer(mhf) {
            @Override
            public SSLSocket createSSLSocket(Socket socket) throws IOException {
                InetSocketAddress remoteAddress = (InetSocketAddress) socket
                        .getRemoteSocketAddress();

                SSLSocketFactory sf = sslContext.getSocketFactory();
                SSLSocket s = (SSLSocket) (sf.createSocket(socket, remoteAddress.getHostName(),
                        socket.getPort(), true));

                // we are a server
                s.setUseClientMode(false);

                // select strong protocols and cipher suites
                s.setEnabledProtocols(s.getSupportedProtocols());
                s.setEnabledCipherSuites(s.getSupportedCipherSuites());

                //// Client must authenticate
                // s.setNeedClientAuth(true);

                return s;
            }
        };

        smtpServer.setHostName("me.amsa.gov.au");
        smtpServer.setPort(25000);
        // smtpServer.setBindAddress(bindAddress);
        smtpServer.setRequireTLS(true);
        smtpServer.start();
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
