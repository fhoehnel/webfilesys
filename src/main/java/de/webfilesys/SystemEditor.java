package de.webfilesys;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


public class SystemEditor extends Thread
{
	String fileName;

	public SystemEditor(String fileName)
	{
		this.fileName=fileName;
	}

	public void run()
	{
		int opSysType = WebFileSys.getInstance().getOpSysType();

		String systemEditor = WebFileSysConfig.getInstance().getSystemEditor();

		try
		{
			if ((opSysType == WebFileSys.OS_OS2) || (opSysType == WebFileSys.OS_WIN)) {
				// launch via "cmd /c start" so the editor window receives
				// foreground activation and appears on top of other windows.
				// The empty string after "start" is the (unused) window title.
				new ProcessBuilder("cmd", "/c", "start", "WebFileSys Editor", systemEditor, fileName).start();
			} else {
				new ProcessBuilder(systemEditor, fileName).start();
			}
		}
		catch (Exception e)
		{
			LogManager.getLogger(getClass()).error(e);
		}
	}
}
