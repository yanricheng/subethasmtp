package org.subethamail.smtp.netty.cmd;

import org.subethamail.smtp.netty.SMTPServerConfig;

public class CmdWrapperBase {
    private SMTPServerConfig smtpServerConfig;

    public SMTPServerConfig getSmtpServerConfig() {
        return smtpServerConfig;
    }

    public void setSmtpServerConfig(SMTPServerConfig smtpServerConfig) {
        this.smtpServerConfig = smtpServerConfig;
    }
}
