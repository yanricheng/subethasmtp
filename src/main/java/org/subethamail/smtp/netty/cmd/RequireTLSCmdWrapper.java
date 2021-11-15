package org.subethamail.smtp.netty.cmd;

import org.subethamail.smtp.DropConnectionException;
import org.subethamail.smtp.internal.server.CommandException;
import org.subethamail.smtp.internal.server.HelpMessage;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;

/**
 * Verifies the presence of a TLS connection if TLS is required.
 * The wrapped command is executed when the test succeeds.
 *
 * @author Erik van Oosten
 */
public final class RequireTLSCmdWrapper extends CmdWrapperBase implements Cmd {

    private final Cmd wrapped;

    /**
     * @param wrapped the wrapped command (not null)
     */
    public RequireTLSCmdWrapper(Cmd wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void execute(String commandString, SmtpSession sess)
            throws IOException, DropConnectionException {
        if (!sess.getSmtpConfig().isRequireTLS() || sess.isTLSStarted())
            wrapped.execute(commandString, sess);
        else
            sess.sendResponse("530 Must issue a STARTTLS command first");
    }

    @Override
    public void execute(SmtpSession sess) throws IOException, DropConnectionException {
        if (!sess.getSmtpConfig().isRequireTLS() || sess.isTLSStarted()) {
            if(wrapped.getSmtpServerConfig()==null) {
                wrapped.setSmtpServerConfig(getSmtpServerConfig());
            }
            wrapped.execute(sess);
        } else {
            sess.sendResponse("530 Must issue a STARTTLS command first");
        }
    }

    @Override
    public HelpMessage getHelp() throws CommandException {
        return wrapped.getHelp();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }
}
