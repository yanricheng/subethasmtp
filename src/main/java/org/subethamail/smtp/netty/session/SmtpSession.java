package org.subethamail.smtp.netty.session;

import io.netty.channel.socket.SocketChannel;
import org.subethamail.smtp.AuthenticationHandler;
import org.subethamail.smtp.netty.SMTPServerConfig;
import org.subethamail.smtp.netty.auth.User;

import java.io.Serializable;
import java.util.Optional;

public class SmtpSession implements Serializable {
    private final SMTPServerConfig smtpServerConfig;
    //是否持续执行之前的命令
    private boolean durativeCmd;
    private String lastCmdName;

    private boolean TLSStarted;
    private SocketChannel channel;
    private Optional<User> user = Optional.empty();
    /**
     * Might exist if the client has successfully authenticated
     */
    private Optional<AuthenticationHandler> authenticationHandler = Optional.empty();
    /**
     * Some state information
     */
    private Optional<String> helo = Optional.empty();
    private boolean authenticated;

    public SmtpSession(SMTPServerConfig smtpServerConfig) {
        this.smtpServerConfig = smtpServerConfig;
    }

    public boolean isDurativeCmd() {
        return durativeCmd;
    }

    public void setDurativeCmd(boolean durativeCmd) {
        this.durativeCmd = durativeCmd;
    }

    public String getLastCmdName() {
        return lastCmdName;
    }

    public void setLastCmdName(String lastCmdName) {
        this.lastCmdName = lastCmdName;
    }

    public Optional<User> getUser() {
        return user;
    }

    public void setUser(Optional<User> user) {
        this.user = user;
    }

    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public SMTPServerConfig getSmtpConfig() {
        return smtpServerConfig;
    }

    public boolean isTLSStarted() {
        return TLSStarted;
    }

    public void setTLSStarted(boolean TLSStarted) {
        this.TLSStarted = TLSStarted;
    }

    public boolean isAuthenticated() {
//        return this.authenticationHandler.isPresent();
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
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
