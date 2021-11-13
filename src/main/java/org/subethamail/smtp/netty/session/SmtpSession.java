package org.subethamail.smtp.netty.session;

import io.netty.channel.socket.SocketChannel;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.netty.SMTPConfig;

import java.util.Optional;

public class SmtpSession {

    private final SMTPConfig smtpConfig;
    private boolean TLSStarted;

    private SocketChannel channel;
    /**
     * Might exist if the client has successfully authenticated
     */
    private Optional<AuthenticationHandler> authenticationHandler = Optional.empty();
    /**
     * Some state information
     */
    private Optional<String> helo = Optional.empty();
    public SmtpSession(SMTPConfig smtpConfig) {
        this.smtpConfig = smtpConfig;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public SMTPConfig getSmtpConfig() {
        return smtpConfig;
    }

    public boolean isTLSStarted() {
        return TLSStarted;
    }

    public void setTLSStarted(boolean TLSStarted) {
        this.TLSStarted = TLSStarted;
    }

    public boolean isAuthenticated() {
        return this.authenticationHandler.isPresent();
    }

    public Optional<AuthenticationHandler> getAuthenticationHandler() {
        return this.authenticationHandler;
    }

    /**
     * This is called by the AuthCommand when a session is successfully
     * authenticated. The handler will be an object created by the
     * AuthenticationHandlerFactory.
     */
    public void setAuthenticationHandler(AuthenticationHandler handler) {
        this.authenticationHandler = Optional.of(handler);
    }

    public void setHelo(String value) {
        this.helo = Optional.of(value);
    }

    public void sendResponse(Object msg) {
        channel.writeAndFlush(msg);
    }

}
