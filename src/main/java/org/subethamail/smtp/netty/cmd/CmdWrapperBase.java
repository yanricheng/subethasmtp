package org.subethamail.smtp.netty.cmd;

import org.subethamail.smtp.netty.SMTPServerConfig;

public class CmdWrapperBase{

    private String commandString;
    private SMTPServerConfig smtpServerConfig;

    public String getCommandString() {
        return commandString;
    }

    public void setCommandString(String commandString) {
        this.commandString = commandString;
    }

    public SMTPServerConfig getSmtpServerConfig() {
        return smtpServerConfig;
    }

    public void setSmtpServerConfig(SMTPServerConfig smtpServerConfig) {
        this.smtpServerConfig = smtpServerConfig;
    }
}
