package de.webfilesys;

import org.apache.log4j.Logger;

public class SystemEditor extends Thread
{
	String fileName;

	public SystemEditor(String fileName)
	{
		this.fileName=fileName;
	}

	public void run()
	{
		String cmd=null;
		Runtime rt=Runtime.getRuntime();

		int opSysType = WebFileSys.getInstance().getOpSysType();
		
		if ((opSysType == WebFileSys.OS_OS2) || (opSysType == WebFileSys.OS_WIN))
		{
			cmd = WebFileSys.getInstance().getSystemEditor() + " \"" + fileName + "\"";
		}
		else
		{
			cmd = WebFileSys.getInstance().getSystemEditor() + " " + fileName;
		}

		try
		{
			rt.exec(cmd);
		}
		catch (Exception e)
		{
			Logger.getLogger(getClass()).error(e);
		}
	}
}
