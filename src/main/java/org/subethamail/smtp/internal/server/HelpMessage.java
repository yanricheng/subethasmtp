package org.subethamail.smtp.internal.server;

import java.util.StringTokenizer;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 */
public final class HelpMessage
{
	private final String commandName;

	private final String argumentDescription;

	private final String helpMessage;

	private final String outputString;

	/** */
	public HelpMessage(String commandName, String helpMessage, String argumentDescription)
	{
		this.commandName = commandName;
		this.argumentDescription = argumentDescription == null ? "" : " " + argumentDescription;
		this.helpMessage = helpMessage;
		StringTokenizer stringTokenizer = new StringTokenizer(this.helpMessage, "\n");
        StringBuilder b = new StringBuilder().append("214-").append(this.commandName).append(this.argumentDescription);
        while (stringTokenizer.hasMoreTokens())
        {
            b.append("\n214-    ").append(stringTokenizer.nextToken());
        }

        b.append("\n214 End of ").append(this.commandName).append(" info");
        this.outputString = b.toString();
	}

	/** */
	public HelpMessage(String commandName, String helpMessage)
	{
		this(commandName, helpMessage, null);
	}

	/** */
	public String getName()
	{
		return this.commandName;
	}

	/** */
	public String toOutputString()
	{
		return this.outputString;
	}

	/** */
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || this.getClass() != o.getClass())
			return false;
		final HelpMessage that = (HelpMessage) o;
		if (this.argumentDescription != null ? !this.argumentDescription.equals(that.argumentDescription)
				: that.argumentDescription != null)
			return false;
		if (this.commandName != null ? !this.commandName.equals(that.commandName)
				: that.commandName != null)
			return false;
		if (this.helpMessage != null ? !this.helpMessage.equals(that.helpMessage)
				: that.helpMessage != null)
			return false;
		return true;
	}

	/** */
	@Override
	public int hashCode()
	{
		int result;
		result = (this.commandName != null ? this.commandName.hashCode() : 0);
		result = 29
				* result
				+ (this.argumentDescription != null ? this.argumentDescription.hashCode()
						: 0);
		result = 29 * result
				+ (this.helpMessage != null ? this.helpMessage.hashCode() : 0);
		return result;
	}
}
