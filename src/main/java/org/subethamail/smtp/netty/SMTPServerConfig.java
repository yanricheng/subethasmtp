package org.subethamail.smtp.netty;


import org.subethamail.smtp.netty.auth.AuthHandlerFactory;

import java.util.Optional;

public class SMTPServerConfig {
    private String hostName = "localhost";
    private String softwareName = "smtp server";
    private int maxMessageSize = 1024;
    private boolean enableTLS = false;
    private boolean hideTLS = true;

    private boolean requireTLS;
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
