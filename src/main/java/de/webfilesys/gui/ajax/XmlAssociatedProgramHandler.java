package de.webfilesys.gui.ajax;

import java.io.PrintWriter;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.AssociationManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlAssociatedProgramHandler extends XmlRequestHandlerBase
{
	public XmlAssociatedProgramHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}
	
	protected void process()
	{
        if (!isAdminUser(false))
        {
            return;
        }
	    
		if (!checkWriteAccess())
		{
			return;
		}
		
        String filePath = getParameter("filePath");

		if (!checkAccess(filePath))
		{
			return;
		}
		
        String progAndParms[] = null;
		
		String assocProg = AssociationManager.getInstance().getAssociatedProgram(filePath);

        if (assocProg == null)
        {
            assocProg = WebFileSys.getInstance().getSystemEditor();
        }

        int spaceIdx = assocProg.indexOf(' ');

        if (spaceIdx>0)
        {
            String parms=assocProg.substring(spaceIdx+1);

            StringTokenizer parmParser=new StringTokenizer(parms);

            progAndParms=new String[parmParser.countTokens() + 2];

            assocProg = assocProg.substring(0,spaceIdx);

            progAndParms[0]=assocProg;

            int i=1;

            while (parmParser.hasMoreTokens())
            {
                progAndParms[i]=parmParser.nextToken();

                i++;
            }

            progAndParms[i]=filePath;
        }
        else
        {
            progAndParms=new String[2];

            progAndParms[0]=assocProg;
            progAndParms[1]=filePath;
        }

        Element resultElement = doc.createElement("result");
        
        Runtime rt=Runtime.getRuntime();

        try
        {
            rt.exec(progAndParms);
            XmlUtil.setChildText(resultElement, "success", "true");
        }
        catch (Exception e)
        {
            Logger.getLogger(getClass()).error("cannot start associated program " + assocProg + " for " + filePath + ": " + e);
            XmlUtil.setChildText(resultElement, "success", "false");
            XmlUtil.setChildText(resultElement, "message", 
                                 getResource("alert.assocProgramFailed", "failed to start associated application") + ": " + assocProg);
        }
			
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
