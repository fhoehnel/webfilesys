package de.webfilesys;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class SystemCmdParms extends Thread
{
	String prog_name_parms[];

	public SystemCmdParms(String prog_name,String parm)
	{
		prog_name_parms=new String[2];
		prog_name_parms[0]=prog_name;
		prog_name_parms[1]=parm;
	}

	public void run()
	{
		Runtime rt=Runtime.getRuntime();

		try
		{
			rt.exec(prog_name_parms);
		}
		catch (Exception e)
		{
			LogManager.getLogger(getClass()).error(e);
		}
	}
}
