package org.subethamail.smtp.netty.cmd;

import org.subethamail.smtp.netty.ServerConfig;

public class CmdWrapperBase {
    private ServerConfig serverConfig;

    private Cmd originCmd;

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }


    public void setOriginCmd(Cmd originCmd) {
        this.originCmd = originCmd;
    }

    public Cmd getOriginCmd() {
        return originCmd;
    }
}
