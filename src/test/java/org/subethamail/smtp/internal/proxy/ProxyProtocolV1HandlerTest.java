package org.subethamail.smtp.internal.proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
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
import org.subethamail.smtp.internal.proxy.ProxyHandler.ProxyResult;
import org.subethamail.smtp.internal.proxy.ProxyProtocolV1Handler.Family;
import org.subethamail.smtp.server.SMTPServer;

/**
 * Tests for {@link ProxyProtocolV2Handler}
 *
 * @author Diego Salvi
 */
public class ProxyProtocolV1HandlerTest {

    @Test
    public void wrongSmallHeader() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        try {
            server.start();

            sendWithProxyCommand(server, "from@localhost", "WRONG");
            fail("An error was expected but it didn't occurred");
        } catch (IOException e) {
            assertTrue(e instanceof SMTPException);
            SMTPException smtpe = (SMTPException) e;
            assertThat(smtpe.getResponse().getCode(), is(ProxyResult.FAIL.errorCode()));
            assertThat(smtpe.getResponse().getMessage(), is(ProxyResult.FAIL.errorMessage()));
        } finally {
            server.stop();
        }
    }

    @Test
    public void wrongBigHeader() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        try {
            server.start();

            String command = "PROXY ";
            while (command.length() < 1000) {
                command += "WRONG";
            }

            sendWithProxyCommand(server, "from@localhost", command);
            fail("An error was expected but it didn't occurred");
        } catch (IOException e) {
            assertTrue(e instanceof SMTPException);
            SMTPException smtpe = (SMTPException) e;
            assertThat(smtpe.getResponse().getCode(), is(ProxyResult.FAIL.errorCode()));
            assertThat(smtpe.getResponse().getMessage(), is(ProxyResult.FAIL.errorMessage()));
        } finally {
            server.stop();
        }
    }

    @Test
    public void wrongFamilyHeader() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        try {
            server.start();

            String command = convert("WRONG",
                    "127.0.0.127", 22222,
                    "127.0.0.1", 2020);

            sendWithProxyCommand(server, "from@localhost", command);
            fail("An error was expected but it didn't occurred");
        } catch (IOException e) {
            assertTrue(e instanceof SMTPException);
            SMTPException smtpe = (SMTPException) e;
            assertThat(smtpe.getResponse().getCode(), is(ProxyResult.FAIL.errorCode()));
            assertThat(smtpe.getResponse().getMessage(), is(ProxyResult.FAIL.errorMessage()));
        } finally {
            server.stop();
        }
    }

    @Test
    public void wrongIPv4PortHeader() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        try {
            server.start();

            String command = convert("TCP4",
                    "127.0.0.127", 99999,
                    "127.0.0.1", 2020);

            sendWithProxyCommand(server, "from@localhost", command);
            fail("An error was expected but it didn't occurred");
        } catch (IOException e) {
            assertTrue(e instanceof SMTPException);
            SMTPException smtpe = (SMTPException) e;
            assertThat(smtpe.getResponse().getCode(), is(ProxyResult.FAIL.errorCode()));
            assertThat(smtpe.getResponse().getMessage(), is(ProxyResult.FAIL.errorMessage()));
        } finally {
            server.stop();
        }
    }

    @Test
    public void wrongIPv4AddressHeader() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        try {
            server.start();

            String command = convert("TCP4",
                    "127A0A0.127", 22222,
                    "127.0.0.1", 2020);

            sendWithProxyCommand(server, "from@localhost", command);
            fail("An error was expected but it didn't occurred");
        } catch (IOException e) {
            assertTrue(e instanceof SMTPException);
            SMTPException smtpe = (SMTPException) e;
            assertThat(smtpe.getResponse().getCode(), is(ProxyResult.FAIL.errorCode()));
            assertThat(smtpe.getResponse().getMessage(), is(ProxyResult.FAIL.errorMessage()));
        } finally {
            server.stop();
        }
    }

    @Test
    public void wrongIPv6ForIPv4AddressHeader() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        try {
            server.start();

            String command = convert("TCP4",
                    "0:0:0:0:0:0:0:127", 22222,
                    "0:0:0:0:0:0:0:1", 2020);

            sendWithProxyCommand(server, "from@localhost", command);
            fail("An error was expected but it didn't occurred");
        } catch (IOException e) {
            assertTrue(e instanceof SMTPException);
            SMTPException smtpe = (SMTPException) e;
            assertThat(smtpe.getResponse().getCode(), is(ProxyResult.FAIL.errorCode()));
            assertThat(smtpe.getResponse().getMessage(), is(ProxyResult.FAIL.errorMessage()));
        } finally {
            server.stop();
        }
    }

    @Test
    public void wrongIPv6PortHeader() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        try {
            server.start();

            String command = convert("TCP6",
                    "0:0:0:0:0:0:0:127", 99999,
                    "0:0:0:0:0:0:0:1", 2020);

            sendWithProxyCommand(server, "from@localhost", command);
            fail("An error was expected but it didn't occurred");
        } catch (IOException e) {
            assertTrue(e instanceof SMTPException);
            SMTPException smtpe = (SMTPException) e;
            assertThat(smtpe.getResponse().getCode(), is(ProxyResult.FAIL.errorCode()));
            assertThat(smtpe.getResponse().getMessage(), is(ProxyResult.FAIL.errorMessage()));
        } finally {
            server.stop();
        }
    }

    @Test
    public void wrongIPv6AddressHeader() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        try {
            server.start();

            String command = convert("TCP6",
                    "0A0A0A0A0A0A0:127", 99999,
                    "0:0:0:0:0:0:0:1", 2020);

            sendWithProxyCommand(server, "from@localhost", command);
            fail("An error was expected but it didn't occurred");
        } catch (IOException e) {
            assertTrue(e instanceof SMTPException);
            SMTPException smtpe = (SMTPException) e;
            assertThat(smtpe.getResponse().getCode(), is(ProxyResult.FAIL.errorCode()));
            assertThat(smtpe.getResponse().getMessage(), is(ProxyResult.FAIL.errorMessage()));
        } finally {
            server.stop();
        }
    }

    @Test
    public void wrongIPv4ForIPv6PortHeader() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        try {
            server.start();

            String command = convert("TCP6",
                    "127.0.0.127", 22222,
                    "127.0.0.1", 2020);

            sendWithProxyCommand(server, "from@localhost", command);
            fail("An error was expected but it didn't occurred");
        } catch (IOException e) {
            assertTrue(e instanceof SMTPException);
            SMTPException smtpe = (SMTPException) e;
            assertThat(smtpe.getResponse().getCode(), is(ProxyResult.FAIL.errorCode()));
            assertThat(smtpe.getResponse().getMessage(), is(ProxyResult.FAIL.errorMessage()));
        } finally {
            server.stop();
        }
    }

    @Test
    public void unknown() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        InetSocketAddress bound;
        try {
            server.start();

            String command = convert(Family.UNKNOWN,
                    new InetSocketAddress("127.0.0.127", 22222),
                    new InetSocketAddress("127.0.0.1", 2020));

            bound = sendWithProxyCommand(server, "from@localhost", command);
        } finally {
            server.stop();
        }

        assertThat(contexts.size(), is(1));
        InetSocketAddress remote = (InetSocketAddress) contexts.get("from@localhost").getRemoteAddress();
        assertThat(remote.getAddress().getHostAddress(), is(bound.getAddress().getHostAddress()));
    }

    @Test
    public void unknownNoData() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        InetSocketAddress bound;
        try {
            server.start();

            String command = convert(Family.UNKNOWN, null, null);

            bound = sendWithProxyCommand(server, "from@localhost", command);
        } finally {
            server.stop();
        }

        assertThat(contexts.size(), is(1));
        InetSocketAddress remote = (InetSocketAddress) contexts.get("from@localhost").getRemoteAddress();
        assertThat(remote.getAddress().getHostAddress(), is(bound.getAddress().getHostAddress()));
    }

    @Test
    public void unknownIPv4NoData() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();
        try {
            server.start();

            sendWithProxyCommand(server, "from@localhost", "PROXY TCP4\r\n");
            fail("An error was expected but it didn't occurred");
        } catch (IOException e) {
            assertTrue(e instanceof SMTPException);
            SMTPException smtpe = (SMTPException) e;
            assertThat(smtpe.getResponse().getCode(), is(ProxyResult.FAIL.errorCode()));
            assertThat(smtpe.getResponse().getMessage(), is(ProxyResult.FAIL.errorMessage()));
        } finally {
            server.stop();
        }
    }

    @Test
    public void unknownIPv6NoData() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();
        try {
            server.start();

            sendWithProxyCommand(server, "from@localhost", "PROXY TCP6\r\n");
            fail("An error was expected but it didn't occurred");
        } catch (IOException e) {
            assertTrue(e instanceof SMTPException);
            SMTPException smtpe = (SMTPException) e;
            assertThat(smtpe.getResponse().getCode(), is(ProxyResult.FAIL.errorCode()));
            assertThat(smtpe.getResponse().getMessage(), is(ProxyResult.FAIL.errorMessage()));
        } finally {
            server.stop();
        }
    }

    @Test
    public void proxyIPv4() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        InetSocketAddress bound;
        try {
            server.start();

            String command = convert(Family.TCP4,
                    new InetSocketAddress("127.0.0.127", 22222),
                    new InetSocketAddress("127.0.0.1", 2020));

            bound = sendWithProxyCommand(server, "from@localhost", command);
        } finally {
            server.stop();
        }

        assertThat(contexts.size(), is(1));
        InetSocketAddress remote = (InetSocketAddress) contexts.get("from@localhost").getRemoteAddress();
        assertThat(remote.getAddress().getHostAddress(), is("127.0.0.127"));
        assertThat(remote.getAddress().getHostAddress(), not(is(bound.getAddress().getHostAddress())));

    }

    @Test
    public void proxyIPv6() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        InetSocketAddress bound;
        try {
            server.start();

            String command = convert(Family.TCP6,
                    new InetSocketAddress("::127", 22222),
                    new InetSocketAddress("::1", 2020));

            bound = sendWithProxyCommand(server, "from@localhost", command);
        } finally {
            server.stop();
        }

        assertThat(contexts.size(), is(1));
        InetSocketAddress remote = (InetSocketAddress) contexts.get("from@localhost").getRemoteAddress();
        assertThat(remote.getAddress().getHostAddress(), is("0:0:0:0:0:0:0:127"));
        assertThat(remote.getAddress().getHostAddress(), not(is(bound.getAddress().getHostAddress())));

    }

    @Test
    public void multiple() throws Exception {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV1Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        InetSocketAddress bound1;
        InetSocketAddress bound2;
        InetSocketAddress bound3;
        InetSocketAddress bound4;
        try {
            server.start();

            String command1 = convert(Family.TCP4,
                    new InetSocketAddress("127.0.0.127", 22222),
                    new InetSocketAddress("127.0.0.1", 2020));

            String command2 = convert(Family.TCP4,
                    new InetSocketAddress("127.0.0.255", 22222),
                    new InetSocketAddress("127.0.0.1", 2020));

            String command3 = convert(Family.TCP6,
                    new InetSocketAddress("::127", 22222),
                    new InetSocketAddress("::1", 2020));

            String command4 = convert(Family.TCP6,
                    new InetSocketAddress("::255", 22222),
                    new InetSocketAddress("::1", 2020));

            Future<InetSocketAddress> f1 = executor.submit(
                    () -> sendWithProxyCommand(server, "from1@localhost", command1));
            Future<InetSocketAddress> f2 = executor.submit(
                    () -> sendWithProxyCommand(server, "from2@localhost", command2));
            Future<InetSocketAddress> f3 = executor.submit(
                    () -> sendWithProxyCommand(server, "from3@localhost", command3));
            Future<InetSocketAddress> f4 = executor.submit(
                    () -> sendWithProxyCommand(server, "from4@localhost", command4));

            bound1 = f1.get();
            bound2 = f2.get();
            bound3 = f3.get();
            bound4 = f4.get();

        } finally {
            executor.shutdownNow();
            server.stop();
        }

        assertThat(contexts.size(), is(4));
        InetSocketAddress remote1 = (InetSocketAddress) contexts.get("from1@localhost").getRemoteAddress();
        assertThat(remote1.getAddress().getHostAddress(), is("127.0.0.127"));
        assertThat(remote1.getAddress().getHostAddress(), not(is(bound1.getAddress().getHostAddress())));
        InetSocketAddress remote2 = (InetSocketAddress) contexts.get("from2@localhost").getRemoteAddress();
        assertThat(remote2.getAddress().getHostAddress(), is("127.0.0.255"));
        assertThat(remote2.getAddress().getHostAddress(), not(is(bound2.getAddress().getHostAddress())));
        InetSocketAddress remote3 = (InetSocketAddress) contexts.get("from3@localhost").getRemoteAddress();
        assertThat(remote3.getAddress().getHostAddress(), is("0:0:0:0:0:0:0:127"));
        assertThat(remote3.getAddress().getHostAddress(), not(is(bound3.getAddress().getHostAddress())));
        InetSocketAddress remote4 = (InetSocketAddress) contexts.get("from4@localhost").getRemoteAddress();
        assertThat(remote4.getAddress().getHostAddress(), is("0:0:0:0:0:0:0:255"));
        assertThat(remote4.getAddress().getHostAddress(), not(is(bound4.getAddress().getHostAddress())));
    }

    static InetSocketAddress sendWithProxyCommand(SMTPServer server, String from, String command) throws IOException {

        MySMTPClient client = new MySMTPClient();
        MySmartClient smart = new MySmartClient(client, "localhost", Optional.empty());

        try {
            client.connect(server.getHostName(), server.getPortAllocated());

            InetSocketAddress boundSocket = (InetSocketAddress) client.getLocalSocketAddress();

            OutputStream sos = client.getSocket().getOutputStream();
            sos.write(command.getBytes(StandardCharsets.US_ASCII));
            sos.flush();

            client.receiveAndCheck(); // The server announces itself first
            smart.sendHeloOrEhlo();

            smart.from(from);
            smart.to("to@localhost");
            smart.dataStart();
            smart.dataWrite("Hello!".getBytes(StandardCharsets.US_ASCII));
            smart.dataEnd();
            smart.quit();

            return boundSocket;
        } catch (SMTPException e) {
            smart.quit();
            throw e;
        } catch (IOException e) {
            client.close(); // just close the socket, issuing QUIT is hopeless now
            throw e;
        }

    }

    /**
     * Creates a PROXY protocol V1 command
     */
    static String convert(Family family, InetSocketAddress src, InetSocketAddress dst) {
        return convert(
                family.name(),
                src == null ? null : src.getAddress().getHostAddress(),
                src == null ? 0 : src.getPort(),
                dst == null ? null : dst.getAddress().getHostAddress(),
                dst == null ? 0 : dst.getPort());
    }

    /**
     * Creates a PROXY protocol V1 command
     */
    static String convert(String family, String asrc, int psrc, String adst, int pdst) {

        switch (family) {
            case "UNKNOWN":
                if (asrc == null && adst == null) {
                    return "PROXY UNKNOWN\r\n";
                }
                if (asrc == null) {
                    if (adst == null) {
                        return "PROXY UNKNOWN\r\n";
                    }
                    throw new IllegalArgumentException("Null source but not null destination");
                } else {
                    if (adst == null) {
                        throw new IllegalArgumentException("Null destination but not null source");
                    }
                    return "PROXY UNKNOWN"
                            + " " + asrc
                            + " " + adst
                            + " " + psrc
                            + " " + pdst
                            + "\r\n";
                }
            case "TCP4":
                if (asrc == null || adst == null) {
                    throw new IllegalArgumentException("Null source or destination");
                }
                return "PROXY TCP4"
                            + " " + asrc
                            + " " + adst
                            + " " + psrc
                            + " " + pdst
                        + "\r\n";
            case "TCP6":
                if (asrc == null || adst == null) {
                    throw new IllegalArgumentException("Null source or destination");
                }
                return "PROXY TCP6"
                            + " " + asrc
                            + " " + adst
                            + " " + psrc
                            + " " + pdst
                        + "\r\n";

            default:
                // Doesn't really support any other family but it needed to test wrong combinations
                return "PROXY " + family
                            + " " + asrc
                            + " " + adst
                            + " " + psrc
                            + " " + pdst
                        + "\r\n";
        }

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
