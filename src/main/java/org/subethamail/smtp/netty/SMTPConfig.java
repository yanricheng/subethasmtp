package org.subethamail.smtp.netty;

import org.subethamail.smtp.AuthenticationHandlerFactory;

import java.util.Optional;

public class SMTPConfig {
    private String hostName = "localhost";
    private int maxMessageSize = 1024;
    private boolean enableTLS = false;
    private boolean hideTLS = true;

    private boolean requireTLS;

    public boolean isRequireTLS() {
        return requireTLS;
    }

    public void setRequireTLS(boolean requireTLS) {
        this.requireTLS = requireTLS;
    }

    /**
     * If true, this server will accept no mail until auth succeeded; ignored if no
     * AuthenticationHandlerFactory has been set
     */
    private boolean requireAuth = false;

    public boolean isRequireAuth() {
        return requireAuth;
    }

    public void setRequireAuth(boolean requireAuth) {
        this.requireAuth = requireAuth;
    }

    private Optional<AuthenticationHandlerFactory> authenticationHandlerFactory = Optional.empty();

    /**
     * Normally you would not want to advertise AUTH related capabilities before a
     * STARTTLS is sent so that credentials do not go in the clear. However, the use
     * of implicit STARTTLS (the property `mail.smtp.starttls.enable` is not set) by
     * a jakarta.mail client (e.g. 1.6.4) prevents AUTH happening. See issue #21. To resolve
     * this issue with jakarta.mail and implicit STARTTLS we provide explicit control
     * over if AUTH capabilities are shown before STARTTLS.
     */
    private boolean showAuthCapabilitiesBeforeSTARTTLS;

    public boolean isShowAuthCapabilitiesBeforeSTARTTLS() {
        return showAuthCapabilitiesBeforeSTARTTLS;
    }

    public void setShowAuthCapabilitiesBeforeSTARTTLS(boolean showAuthCapabilitiesBeforeSTARTTLS) {
        this.showAuthCapabilitiesBeforeSTARTTLS = showAuthCapabilitiesBeforeSTARTTLS;
    }

    public Optional<AuthenticationHandlerFactory> getAuthenticationHandlerFactory() {
        return authenticationHandlerFactory;
    }

    public void setAuthenticationHandlerFactory(Optional<AuthenticationHandlerFactory> authenticationHandlerFactory) {
        this.authenticationHandlerFactory = authenticationHandlerFactory;
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
