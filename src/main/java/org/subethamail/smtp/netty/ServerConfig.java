package org.subethamail.smtp.netty;


import com.github.davidmoten.guavamini.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.netty.auth.AuthHandlerFactory;
import org.subethamail.smtp.netty.mail.handler.BasicMsgHandlerFactory;
import org.subethamail.smtp.netty.mail.handler.MsgHandlerFactory;
import org.subethamail.smtp.netty.mail.handler.SimpleMsgListenerAdapter;
import org.subethamail.smtp.netty.mail.listener.SimpleMsgListener;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class ServerConfig {

    private final static Logger logger = LoggerFactory.getLogger(ServerConfig.class);
    private final static int MAX_MESSAGE_SIZE_UNLIMITED = 0;
    private static final MsgHandlerFactory MESSAGE_HANDLER_FACTORY_DEFAULT = new BasicMsgHandlerFactory(
            (context, from, to, data) -> logger.info("From: " + from + ", To: " + to + "\n"
                    + new String(data, StandardCharsets.UTF_8) + "\n--------END OF MESSAGE ------------"),
            MAX_MESSAGE_SIZE_UNLIMITED);
    private String hostName = "localhost";
    private String softwareName = "smtp server";
    private int maxMessageSize = 1024;
    private boolean enableTLS = false;
    private boolean hideTLS = true;
    private int maxRecipients = 50;
    private boolean requireTLS;
    private boolean disableReceivedHeaders;
    private boolean disableVerify = true;
    private String domain;
    /**
     * If true, this server will accept no mail until auth succeeded; ignored if no
     * AuthHandlerFactory has been set
     */
    private boolean requireAuth = false;
    private Optional<AuthHandlerFactory> authHandlerFactory = Optional.empty();
    /**
     * Normally you would not want to advertise AUTH related capabilities before a
     * STARTTLS is sent so that credentials do not go in the clear. However, the use
     * of implicit STARTTLS (the property `mail.smtp.starttls.enable` is not set) by
     * a jakarta.mail client (e.g. 1.6.4) prevents AUTH happening. See issue #21. To resolve
     * this issue with jakarta.mail and implicit STARTTLS we provide explicit control
     * over if AUTH capabilities are shown before STARTTLS.
     */
    private boolean showAuthCapabilitiesBeforeSTARTTLS;
    private MsgHandlerFactory messageHandlerFactory = MESSAGE_HANDLER_FACTORY_DEFAULT;

    public void messageHandlerFactory(MsgHandlerFactory factory) {
        Preconditions.checkNotNull(factory);
        Preconditions.checkArgument(this.messageHandlerFactory == MESSAGE_HANDLER_FACTORY_DEFAULT,
                "can only set message handler factory once");
        this.messageHandlerFactory = factory;
    }

    public void simpleMessageListener(SimpleMsgListener listener) {
        this.messageHandlerFactory = new SimpleMsgListenerAdapter(listener);
    }
    public void simpleMessageListeners(List<SimpleMsgListener> listeners) {
        this.messageHandlerFactory = new SimpleMsgListenerAdapter(listeners);
    }

    public MsgHandlerFactory getMessageHandlerFactory() {
        return messageHandlerFactory;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isDisableVerify() {
        return disableVerify;
    }

    public void setDisableVerify(boolean disableVerify) {
        this.disableVerify = disableVerify;
    }

    public boolean isDisableReceivedHeaders() {
        return disableReceivedHeaders;
    }

    public void setDisableReceivedHeaders(boolean disableReceivedHeaders) {
        this.disableReceivedHeaders = disableReceivedHeaders;
    }

    public int getMaxRecipients() {
        return maxRecipients;
    }

    public void setMaxRecipients(int maxRecipients) {
        this.maxRecipients = maxRecipients;
    }

    public String getSoftwareName() {
        return softwareName;
    }

    public void setSoftwareName(String softwareName) {
        this.softwareName = softwareName;
    }

    public boolean isRequireTLS() {
        return requireTLS;
    }

    public void setRequireTLS(boolean requireTLS) {
        this.requireTLS = requireTLS;
    }

    public boolean isRequireAuth() {
        return requireAuth;
    }

    public void setRequireAuth(boolean requireAuth) {
        this.requireAuth = requireAuth;
    }

    public boolean isShowAuthCapabilitiesBeforeSTARTTLS() {
        return showAuthCapabilitiesBeforeSTARTTLS;
    }

    public void setShowAuthCapabilitiesBeforeSTARTTLS(boolean showAuthCapabilitiesBeforeSTARTTLS) {
        this.showAuthCapabilitiesBeforeSTARTTLS = showAuthCapabilitiesBeforeSTARTTLS;
    }

    public Optional<AuthHandlerFactory> getAuthHandlerFactory() {
        return authHandlerFactory;
    }

    public void setAuthHandlerFactory(Optional<AuthHandlerFactory> authHandlerFactory) {
        this.authHandlerFactory = authHandlerFactory;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    public boolean isEnableTLS() {
        return enableTLS;
    }

    public void setEnableTLS(boolean enableTLS) {
        this.enableTLS = enableTLS;
    }

    public boolean isHideTLS() {
        return hideTLS;
    }

    public void setHideTLS(boolean hideTLS) {
        this.hideTLS = hideTLS;
    }
}
