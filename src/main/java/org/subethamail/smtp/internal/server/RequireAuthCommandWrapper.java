package org.subethamail.smtp.internal.server;

import org.subethamail.smtp.DropConnectionException;
import org.subethamail.smtp.server.Session;

import java.io.IOException;

/**
 * Thin wrapper around any command to make sure authentication
 * has been performed.
 *
 * @author Evgeny Naumenko
 */
public  final class RequireAuthCommandWrapper implements Command
{

    private final Command wrapped;

    /**
     * @param wrapped the wrapped command (not null)
     */
    public RequireAuthCommandWrapper(Command wrapped)
    {
        this.wrapped = wrapped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(String commandString, Session sess)
            throws IOException, DropConnectionException
    {
        if (!sess.getServer().getRequireAuth() || sess.isAuthenticated())
            wrapped.execute(commandString, sess);
        else
            sess.sendResponse("530 5.7.0  Authentication required");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HelpMessage getHelp() throws CommandException
    {
        return wrapped.getHelp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return wrapped.getName();
    }
}
