package de.webfilesys.gui.ajax;

import java.io.BufferedWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.webfilesys.WebFileSys;
import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 *
 */
public class XmlRequestHandlerBase extends UserRequestHandler
{
	protected Document doc;

	private DocumentBuilder builder;

	Element resourcesElement = null;

	Element requestParmsElement = null;
	
	HttpServletResponse resp = null;
	
	public XmlRequestHandlerBase(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
        
        this.resp = resp;

		builder = null;

		try
		{
			builder = WebFileSys.getInstance().getDocFactory().newDocumentBuilder();

			doc = builder.newDocument();
		}
		catch (ParserConfigurationException pcex)
		{
			Logger.getLogger(getClass()).error(pcex.toString());
			System.out.println(pcex.toString());
		}
	}

	protected void addMsgResource(String key, String value)
	{
		if (resourcesElement == null)
		{
			resourcesElement = doc.createElement("resources");
			
			doc.getDocumentElement().appendChild(resourcesElement);
		}
		
		Element msgElement = doc.createElement("msg");
		
		resourcesElement.appendChild(msgElement);
		
		msgElement.setAttribute("key", key);
		msgElement.setAttribute("value", value);
	}

	protected void processResponse()
	{
		resp.setContentType("text/xml");

		BufferedWriter xmlOutFile = new BufferedWriter(output);
                
		XmlUtil.writeToStream(doc, xmlOutFile);

		output.flush();
	}
}
