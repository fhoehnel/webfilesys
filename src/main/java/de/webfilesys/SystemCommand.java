package de.webfilesys;

import org.apache.log4j.Logger;

public class SystemCommand extends Thread
{
	String prog_name;

	public SystemCommand(String prog_name)
	{
		this.prog_name=prog_name;
	}

	public void run()
	{
		Runtime rt=Runtime.getRuntime();

		try
		{
			rt.exec(prog_name);
		}
		catch (Exception e)
		{
			Logger.getLogger(getClass()).warn(e);
		}
	}
}
