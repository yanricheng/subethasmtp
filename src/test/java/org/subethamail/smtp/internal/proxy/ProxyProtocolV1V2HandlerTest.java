package org.subethamail.smtp.internal.proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.client.Authenticator;
import org.subethamail.smtp.client.SMTPClient;
import org.subethamail.smtp.client.SMTPException;
import org.subethamail.smtp.client.SmartClient;
import org.subethamail.smtp.helper.BasicMessageListener;
import org.subethamail.smtp.server.SMTPServer;

/**
 * Tests for {@link ProxyProtocolV2Handler}
 *
 * @author Diego Salvi
 */
public class ProxyProtocolV1V2HandlerTest {

    @Test
    public void v1() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1V2Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        InetSocketAddress bound;
        try {
            server.start();

            String command = ProxyProtocolV1HandlerTest.convert(
                    ProxyProtocolV1Handler.Family.TCP4,
                    new InetSocketAddress("127.0.0.127", 22222),
                    new InetSocketAddress("127.0.0.1", 2020));

            bound = ProxyProtocolV1HandlerTest.sendWithProxyCommand(server, "from@localhost", command);
        } finally {
            server.stop();
        }

        assertThat(contexts.size(), is(1));
        InetSocketAddress remote = (InetSocketAddress) contexts.get("from@localhost").getRemoteAddress();
        assertThat(remote.getAddress().getHostAddress(), is("127.0.0.127"));
        assertThat(remote.getAddress().getHostAddress(), not(is(bound.getAddress().getHostAddress())));

    }

    @Test
    public void v2() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1V2Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        InetSocketAddress bound;
        try {
            server.start();

            byte[] command = ProxyProtocolV2HandlerTest.convert(
                    ProxyProtocolV2Handler.Command.PROXY,
                    ProxyProtocolV2Handler.Family.INET,
                    ProxyProtocolV2Handler.Transport.STREAM,
                    new InetSocketAddress("127.0.0.127", 22222),
                    new InetSocketAddress("127.0.0.1", 2020));

            bound = ProxyProtocolV2HandlerTest.sendWithProxyCommand(server, "from@localhost", command);
        } finally {
            server.stop();
        }

        assertThat(contexts.size(), is(1));
        InetSocketAddress remote = (InetSocketAddress) contexts.get("from@localhost").getRemoteAddress();
        assertThat(remote.getAddress().getHostAddress(), is("127.0.0.127"));
        assertThat(remote.getAddress().getHostAddress(), not(is(bound.getAddress().getHostAddress())));
    }

    @Test
    public void multiple() throws Exception {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1V2Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        InetSocketAddress bound1;
        InetSocketAddress bound2;
        try {
            server.start();

            String command1 = ProxyProtocolV1HandlerTest.convert(
                    ProxyProtocolV1Handler.Family.TCP4,
                    new InetSocketAddress("127.0.0.127", 22222),
                    new InetSocketAddress("127.0.0.1", 2020));

            byte[] command2 = ProxyProtocolV2HandlerTest.convert(
                    ProxyProtocolV2Handler.Command.PROXY,
                    ProxyProtocolV2Handler.Family.INET,
                    ProxyProtocolV2Handler.Transport.STREAM,
                    new InetSocketAddress("127.0.0.255", 22222),
                    new InetSocketAddress("127.0.0.1", 2020));

            Future<InetSocketAddress> f1 = executor.submit(
                    () -> ProxyProtocolV1HandlerTest.sendWithProxyCommand(server, "from1@localhost", command1));
            Future<InetSocketAddress> f2 = executor.submit(
                    () -> ProxyProtocolV2HandlerTest.sendWithProxyCommand(server, "from2@localhost", command2));

            bound1 = f1.get();
            bound2 = f2.get();

        } finally {
            executor.shutdownNow();
            server.stop();
        }

        assertThat(contexts.size(), is(2));
        InetSocketAddress remote1 = (InetSocketAddress) contexts.get("from1@localhost").getRemoteAddress();
        assertThat(remote1.getAddress().getHostAddress(), is("127.0.0.127"));
        assertThat(remote1.getAddress().getHostAddress(), not(is(bound1.getAddress().getHostAddress())));
        InetSocketAddress remote2 = (InetSocketAddress) contexts.get("from2@localhost").getRemoteAddress();
        assertThat(remote2.getAddress().getHostAddress(), is("127.0.0.255"));
        assertThat(remote2.getAddress().getHostAddress(), not(is(bound2.getAddress().getHostAddress())));
    }

    public static final class MySMTPClient extends SMTPClient {

        private Socket socket;

        public MySMTPClient() {
            super();
        }

        public MySMTPClient(Optional<SocketAddress> bindpoint, Optional<String> hostPortName) {
            super(bindpoint, hostPortName);
        }

        @Override
        protected Socket createSocket() {
            this.socket = super.createSocket();
            return socket;
        }

        public Socket getSocket() {
            return socket;
        }

    }

    public static final class MySmartClient extends SmartClient {

        public MySmartClient(SMTPClient client, String clientHeloHost, Optional<Authenticator> authenticator)
                throws IOException, SMTPException {
            super(client, clientHeloHost, authenticator);
        }

        @Override
        public void sendHeloOrEhlo() throws IOException, SMTPException {
            super.sendHeloOrEhlo();
        }

    }

}
