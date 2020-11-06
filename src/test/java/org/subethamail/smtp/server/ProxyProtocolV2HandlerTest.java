package org.subethamail.smtp.server;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
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
import org.subethamail.smtp.internal.proxy.ProxyHandler;
import org.subethamail.smtp.internal.proxy.ProxyProtocolV2Handler;
import org.subethamail.smtp.internal.proxy.ProxyProtocolV2Handler.Command;
import org.subethamail.smtp.internal.proxy.ProxyProtocolV2Handler.Family;
import org.subethamail.smtp.internal.proxy.ProxyProtocolV2Handler.Transport;

/**
 * Tests for {@link ProxyProtocolV2Handler}
 *
 * @author Diego Salvi
 */
public class ProxyProtocolV2HandlerTest {

    @Test
    public void wrongSmallHeader() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV2Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        InetSocketAddress bound;
        try {
            server.start();

            bound = sendWithProxyCommand(server, "from@localhost", new byte[4]);
            fail("An error was expected but it didn't occurred");
        } catch (IOException e) {
            assertTrue(e instanceof SMTPException);
            SMTPException smtpe = (SMTPException) e;
            assertThat(smtpe.getResponse().getCode(), is(ProxyHandler.ProxyResult.FAIL.errorCode()));
            assertThat(smtpe.getResponse().getMessage(), is(ProxyHandler.ProxyResult.FAIL.errorMessage()));
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
                .proxyHandler(ProxyProtocolV2Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        InetSocketAddress bound;
        try {
            server.start();

            bound = sendWithProxyCommand(server, "from@localhost", new byte[1000]);
            fail("An error was expected but it didn't occurred");
        } catch (IOException e) {
            assertTrue(e instanceof SMTPException);
            SMTPException smtpe = (SMTPException) e;
            assertThat(smtpe.getResponse().getCode(), is(ProxyHandler.ProxyResult.FAIL.errorCode()));
            assertThat(smtpe.getResponse().getMessage(), is(ProxyHandler.ProxyResult.FAIL.errorMessage()));
        } finally {
            server.stop();
        }
    }

    @Test
    public void local() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV2Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        InetSocketAddress bound;
        try {
            server.start();

            bound = sendWithProxyCommand(server, "from@localhost", LOCAL_COMMAND);
        } finally {
            server.stop();
        }

        assertThat(contexts.size(), is(1));
        InetSocketAddress remote = (InetSocketAddress) contexts.get("from@localhost").getRemoteAddress();
        assertThat(remote.getAddress().getHostAddress(), is(bound.getAddress().getHostAddress()));
    }

    @Test
    public void proxy() throws IOException {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV2Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        InetSocketAddress bound;
        try {
            server.start();

            byte[] command = convert(Command.PROXY, Family.INET, Transport.STREAM,
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
    public void multiple() throws Exception {

        Map<String, MessageContext> contexts = new ConcurrentHashMap<>();
        BasicMessageListener listener = (context, from, to, data) -> contexts.put(from, context);

        SMTPServer server = SMTPServer
                .port(2020)
                .proxyHandler(ProxyProtocolV2Handler.INSTANCE)
                .messageHandler(listener)
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        InetSocketAddress bound1;
        InetSocketAddress bound2;
        try {
            server.start();

            byte[] command1 = convert(Command.PROXY, Family.INET, Transport.STREAM,
                    new InetSocketAddress("127.0.0.127", 22222),
                    new InetSocketAddress("127.0.0.1", 2020));

            byte[] command2 = convert(Command.PROXY, Family.INET, Transport.STREAM,
                    new InetSocketAddress("127.0.0.255", 22222),
                    new InetSocketAddress("127.0.0.1", 2020));

            Future<InetSocketAddress> f1 = executor.submit(
                    () -> sendWithProxyCommand(server, "from1@localhost", command1));
            Future<InetSocketAddress> f2 = executor.submit(
                    () -> sendWithProxyCommand(server, "from2@localhost", command2));

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

    static InetSocketAddress sendWithProxyCommand(SMTPServer server, String from, byte[] command) throws IOException {

        MySMTPClient client = new MySMTPClient();
        MySmartClient smart = new MySmartClient(client, "localhost", Optional.empty());

        try {
            client.connect(server.getHostName(), server.getPortAllocated());

            InetSocketAddress boundSocket = (InetSocketAddress) client.getLocalSocketAddress();

            OutputStream sos = client.getSocket().getOutputStream();
            sos.write(command);
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

    private static final byte[] PROXY_MAGIC =
            new byte[]{0x0D, 0x0A, 0x0D, 0x0A, 0x00, 0x0D, 0x0A, 0x51, 0x55, 0x49, 0x54, 0x0A};

    private static final byte[] UNSPEC_COMMAND = convert(Command.PROXY, Family.UNSPEC, Transport.UNSPEC, null, null);
    private static final byte[] LOCAL_COMMAND = convert(Command.LOCAL, Family.UNSPEC, Transport.UNSPEC, null, null);

    /**
     * Creates a PROXY protocol V2 command
     */
    static byte[] convert(Command command, Family family, Transport transport,
                          InetSocketAddress src, InetSocketAddress dst) {

        switch (command) {
            case LOCAL: {
                byte[] data = new byte[16];
                System.arraycopy(PROXY_MAGIC, 0, data, 0, PROXY_MAGIC.length);
                data[PROXY_MAGIC.length] = (byte) (0x20 | command.value); // Version and command
                return data;
            }
            case PROXY: {

                int addresslen;
                byte[] srca;
                byte[] dsta;

                switch (family) {
                    case UNSPEC:
                        byte[] data = new byte[16];
                        System.arraycopy(PROXY_MAGIC, 0, data, 0, PROXY_MAGIC.length);
                        data[PROXY_MAGIC.length] = (byte) (0x20 | command.value); // Version and command
                        return data;

                    case INET:
                        srca = src.getAddress().getAddress();
                        dsta = dst.getAddress().getAddress();
                        addresslen = 4;
                        break;

                    case INET6:
                        srca = src.getAddress().getAddress();
                        dsta = dst.getAddress().getAddress();
                        addresslen = 16;
                        break;

                    case UNIX:
                        throw new IllegalArgumentException("Unix socket not supported");

                    default:
                        throw new IllegalArgumentException("Unknown family " + family);
                }

                if (!src.getClass().equals(dst.getClass())) {
                    throw new IllegalArgumentException("Socked addresses mismatch");
                }

                if (transport.value < 0 || transport.value > 2) {
                    throw new IllegalArgumentException("Unknown transport " + transport);
                }

                if (srca.length != addresslen) {
                    throw new IllegalArgumentException("Source addres type mismatch");
                }
                if (dsta.length != addresslen) {
                    throw new IllegalArgumentException("Destination addres type mismatch");
                }

                byte[] data = new byte[16 + addresslen * 2 + 4];
                System.arraycopy(PROXY_MAGIC, 0, data, 0, PROXY_MAGIC.length);

                data[PROXY_MAGIC.length] = (byte) (0x20 | command.value); // Version and command
                data[PROXY_MAGIC.length + 1] = (byte) ((family.value << 4) | transport.value);

                writeShort(addresslen * 2 + 4, data, PROXY_MAGIC.length + 2);

                System.arraycopy(srca, 0, data, PROXY_MAGIC.length + 4, addresslen);
                System.arraycopy(dsta, 0, data, PROXY_MAGIC.length + 4 + addresslen, addresslen);

                int srcp = src.getPort();
                int dstp = dst.getPort();

                writeShort(srcp, data, PROXY_MAGIC.length + 4 + addresslen * 2);
                writeShort(dstp, data, PROXY_MAGIC.length + 4 + addresslen * 2 + 2);

                return data;
            }
            default:
                throw new IllegalArgumentException("Unknown command " + command);
        }

    }

    private static void writeShort(int data, byte[] array, int offset) {
        data = data & 0xFFFF;
        array[offset] = (byte) (data >> Byte.SIZE);
        array[offset + 1] = (byte) (data & 0xFF);
    }

    public static final class MySMTPClient extends SMTPClient {

        private Socket socket;

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
