package de.webfilesys;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


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
			LogManager.getLogger(getClass()).warn(e);
		}
	}
}
