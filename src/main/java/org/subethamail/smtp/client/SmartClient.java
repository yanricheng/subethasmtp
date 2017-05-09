package org.subethamail.smtp.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.client.SMTPClient.Response;

/**
 * A somewhat smarter abstraction of an SMTP client which doesn't require
 * knowing anything about the nitty gritty of SMTP.
 *
 * @author Jeff Schnitzer
 */
// not final so can mock with Mockito
public class SmartClient  {
    /** */
    private static final Logger log = LoggerFactory.getLogger(SmartClient.class);

    /** */
    private boolean sentFrom;
    int recipientCount;
    /** The host name which is sent in the HELO and EHLO commands */
    private String heloHost;

    /**
     * True if the server sent a 421
     * "Service not available, closing transmission channel" response. In this
     * case the QUIT command should not be sent.
     */
    private boolean serverClosingTransmissionChannel = false;

    /**
     * SMTP extensions supported by the server, and their parameters as the
     * server specified it in response to the EHLO command. Key is the extension
     * keyword in upper case, like "AUTH", value is the extension parameters
     * string in unparsed form. If the server does not support EHLO, then this
     * map is empty.
     */
    private final Map<String, String> extensions = new HashMap<String, String>();

    /**
     * If supplied (not null), then it will be called after EHLO, to
     * authenticate this client to the server.
     */
    private Authenticator authenticator = null;

    private SMTPClient client;

    /**
     * Connects to the specified server and issues the initial HELO command.
     * 
     * @throws UnknownHostException
     *             if problem looking up hostname
     * @throws SMTPException
     *             if problem reported by the server
     * @throws IOException
     *             if problem communicating with host
     */
    private SmartClient(String myHost)
            throws UnknownHostException, IOException, SMTPException {
        this(null, myHost);
    }
    
    /**
     * Connects to the specified server and issues the initial HELO command.
     * 
     * @throws UnknownHostException
     *             if problem looking up hostname
     * @throws SMTPException
     *             if problem reported by the server
     * @throws IOException
     *             if problem communicating with host
     */
    private SmartClient(SocketAddress bindpoint, String myHost)
            throws UnknownHostException, IOException, SMTPException {
        client = new SMTPClient(bindpoint);
        this.setHeloHost(myHost);
    }
   
    public final static SmartClient createAndConnect(String host, int port, String myHost) throws UnknownHostException, SMTPException, IOException {
        return createAndConnect(host, port, null, myHost);
    }
    
    public final static SmartClient createAndConnect(String host, int port, SocketAddress bindpoint, String myHost) throws UnknownHostException, SMTPException, IOException {
        SmartClient client = new SmartClient(bindpoint, myHost);
        client.connect(host, port);
        return client;
    }
    
    /**
     * Connects to the specified server and issues the initial HELO command. It
     * gracefully closes the connection if it could be established but
     * subsequently it fails or if the server does not accept messages.
     */
    public void connect(String host, int port)
            throws SMTPException, AuthenticationNotSupportedException, IOException {
        if (heloHost == null)
            throw new IllegalStateException("Helo host must be specified before connecting");

        client.connect(host, port);
        try {
            client.receiveAndCheck(); // The server announces itself first
            this.sendHeloOrEhlo();
            if (this.authenticator != null)
                this.authenticator.authenticate();
        } catch (SMTPException e) {
            this.quit();
            throw e;
        } catch (AuthenticationNotSupportedException e) {
            this.quit();
            throw e;
        } catch (IOException e) {
            client.close(); // just close the socket, issuing QUIT is hopeless now
            throw e;
        }
    }

    /**
     * Sends the EHLO command, or HELO if EHLO is not supported, and saves the
     * list of SMTP extensions which are supported by the server.
     */
    protected void sendHeloOrEhlo() throws IOException, SMTPException {
        extensions.clear();
        Response resp = client.sendReceive("EHLO " + heloHost);
        if (resp.isSuccess()) {
            parseEhloResponse(resp);
        } else if (resp.getCode() == 500 || resp.getCode() == 502) {
            // server does not support EHLO, try HELO
            client.sendAndCheck("HELO " + heloHost);
        } else {
            // some serious error
            throw new SMTPException(resp);
        }
    }

    /**
     * Extracts the list of SMTP extensions from the server's response to EHLO,
     * and stores them in {@link #extensions}.
     */
    private void parseEhloResponse(Response resp) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(resp.getMessage()));
        // first line contains server name and welcome message, skip it
        reader.readLine();
        String line;
        while (null != (line = reader.readLine())) {
            int iFirstSpace = line.indexOf(' ');
            String keyword = iFirstSpace == -1 ? line : line.substring(0, iFirstSpace);
            String parameters = iFirstSpace == -1 ? "" : line.substring(iFirstSpace + 1);
            extensions.put(keyword.toUpperCase(Locale.ENGLISH), parameters);
        }
    }

    /**
     * Returns the server response. It takes note of a 421 response code, so
     * QUIT will not be issued unnecessarily.
     */
    protected Response receive() throws IOException {
        Response response = client.receive();
        if (response.getCode() == 421)
            serverClosingTransmissionChannel = true;
        return response;
    }

    /** */
    public void from(String from) throws IOException, SMTPException {
        client.sendAndCheck("MAIL FROM: <" + from + ">");
        this.sentFrom = true;
    }

    /** */
    public void to(String to) throws IOException, SMTPException {
        client.sendAndCheck("RCPT TO: <" + to + ">");
        this.recipientCount++;
    }

    /**
     * Prelude to writing data
     */
    public void dataStart() throws IOException, SMTPException {
        client.sendAndCheck("DATA");
    }

    /**
     * Actually write some data
     */
    public void dataWrite(byte[] data, int numBytes) throws IOException {
        client.dataOutput.write(data, 0, numBytes);
    }

    /**
     * Last step after writing data
     */
    public void dataEnd() throws IOException, SMTPException {
        client.dataOutput.flush();
        client.dotTerminatedOutput.writeTerminatingSequence();
        client.dotTerminatedOutput.flush();

        client.receiveAndCheck();
    }

    /**
     * Quit and close down the connection. Ignore any errors.
     * <p>
     * It still closes the connection, but it does not send the QUIT command if
     * a 421 Service closing transmission channel is received previously. In
     * these cases QUIT would fail anyway.
     * 
     * @see <a href="http://tools.ietf.org/html/rfc5321#section-3.8">RFC 5321
     *      Terminating Sessions and Connections</a>
     */
    public void quit() {
        try {
            if (client.isConnected() && !this.serverClosingTransmissionChannel)
                client.sendAndCheck("QUIT");
        } catch (IOException ex) {
            log.warn("Failed to issue QUIT to " + client.getHostPort());
        }

        client.close();
    }

    /**
     * @return true if we have already specified from()
     */
    public boolean sentFrom() {
        return this.sentFrom;
    }

    /**
     * @return true if we have already specified to()
     */
    public boolean sentTo() {
        return this.recipientCount > 0;
    }

    /**
     * @return the number of recipients that have been accepted by the server
     */
    public int getRecipientCount() {
        return this.recipientCount;
    }

    /**
     * Returns the SMTP extensions supported by the server.
     * 
     * @return the extension map. Key is the extension keyword in upper case,
     *         value is the unparsed string of extension parameters.
     */
    public Map<String, String> getExtensions() {
        return extensions;
    }

    /**
     * Sets the domain name or address literal of this system, which name will
     * be sent to the server in the parameter of the HELO and EHLO commands.
     * This has no default and is required.
     */
    public void setHeloHost(String myHost) {
        this.heloHost = myHost;
    }

    /**
     * Returns the HELO name of this system.
     */
    public String getHeloHost() {
        return heloHost;
    }

    /**
     * Returns the Authenticator object, which is used to authenticate this
     * client to the server, or null, if no authentication is required.
     */
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    /**
     * Sets the Authenticator object which will be called after the EHLO command
     * to authenticate this client to the server. The default is that no
     * authentication will happen.
     */
    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void sendAndCheck(String msg) throws SMTPException, IOException {
        client.sendAndCheck(msg);
    }
}
