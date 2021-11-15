package org.subethamail.smtp.netty.cmd;

import org.subethamail.smtp.DropConnectionException;
import org.subethamail.smtp.internal.server.CommandException;
import org.subethamail.smtp.internal.server.HelpMessage;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;

/**
 * Thin wrapper around any command to make sure authentication
 * has been performed.
 *
 * @author Evgeny Naumenko
 */
public final class RequireAuthCmdWrapper extends CmdWrapperBase implements Cmd {

    private final Cmd wrapped;

    /**
     * @param wrapped the wrapped command (not null)
     */
    public RequireAuthCmdWrapper(Cmd wrapped) {
        this.wrapped = wrapped;
    }


    @Override
    public void execute(String commandString, SmtpSession sess) throws IOException, DropConnectionException {
        if (!sess.getSmtpConfig().isRequireAuth() || sess.isAuthenticated()) {
            if (wrapped.getCommandString() == null) {
                wrapped.setCommandString(getCommandString());
            }
            if (wrapped.getSmtpServerConfig() == null) {
                wrapped.setSmtpServerConfig(getSmtpServerConfig());
            }
            wrapped.execute(commandString, sess);
        } else {
            sess.sendResponse("530 5.7.0  Authentication required");
        }
    }

    @Override
    public void execute(SmtpSession sess) throws IOException, DropConnectionException {
        if (!sess.getSmtpConfig().isRequireAuth() || sess.isAuthenticated())
            wrapped.execute(sess);
        else
            sess.sendResponse("530 5.7.0  Authentication required");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HelpMessage getHelp() throws CommandException {
        return wrapped.getHelp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return wrapped.getName();
    }
}
