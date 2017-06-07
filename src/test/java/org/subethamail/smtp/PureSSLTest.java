package org.subethamail.smtp;

import static org.subethamail.smtp.TestUtil.EMAIL_FROM;
import static org.subethamail.smtp.TestUtil.EMAIL_TO;
import static org.subethamail.smtp.TestUtil.PORT;
import static org.subethamail.smtp.TestUtil.createTlsSslContext;
import static org.subethamail.smtp.TestUtil.getKeyManagers;
import static org.subethamail.smtp.TestUtil.getTrustManagers;
import static org.subethamail.smtp.TestUtil.send;

import java.io.InputStream;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.subethamail.smtp.TestUtil.ConnectionType;
import org.subethamail.smtp.server.SMTPServer;

public class PureSSLTest {

    private static final String SERVER_HOSTNAME = "email-server.me.com";

    @Test(timeout = 3000)
    public void testRunningSMTPServerOnSSLSocket() throws Exception {
        KeyManager[] keyManagers = getKeyManagers();
        TrustManager[] trustManagers = getTrustManagers();
        SSLContext sslContext = createTlsSslContext(keyManagers, trustManagers);

        // mock a MessageHandlerFactory to check for delivery
        MessageHandlerFactory mhf = Mockito.mock(MessageHandlerFactory.class);
        MessageHandler mh = Mockito.mock(MessageHandler.class);
        Mockito.when(mhf.create(ArgumentMatchers.any(MessageContext.class))).thenReturn(mh);

        SMTPServer server = SMTPServer //
                .port(PORT) //
                .hostName(SERVER_HOSTNAME) //
                .messageHandlerFactory(mhf) //
                .executorService(Executors.newSingleThreadExecutor()) //
                .serverSocketFactory(sslContext) //
                .backlog(10) //
                .connectionTimeoutMs(30000) //
                .maxConnections(20) //
                .build();
        try {
            server.start();
            send(trustManagers, ConnectionType.PURE_TLS);
        } finally {
            server.stop();
        }
        InOrder o = Mockito.inOrder(mhf, mh);
        o.verify(mhf).create(ArgumentMatchers.any(MessageContext.class));
        o.verify(mh).from(EMAIL_FROM);
        o.verify(mh).recipient(EMAIL_TO);
        o.verify(mh).data(ArgumentMatchers.any(InputStream.class));
        o.verify(mh).done();
        o.verifyNoMoreInteractions();
    }

}
