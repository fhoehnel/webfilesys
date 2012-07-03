package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * Show geographic location of all pictures of the directory in open street map.
 * @author Frank Hoehnel
 */
public class XslOpenStreetMapFilesHandler extends XslRequestHandlerBase
{
	public XslOpenStreetMapFilesHandler(
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
		String path = getParameter("path");

		if (!accessAllowed(path))
		{
			Logger.getLogger(getClass()).warn("user " + uid + " tried to access folder outside of his document root: " + path);
			
			return;
		}
		
		File folder = new File(path);
		
		if (!folder.exists())
		{
			Logger.getLogger(getClass()).error("folder not found: " + path);
			
			return;
		}
		
        String shortPath = CommonUtils.shortName(getHeadlinePath(path), 50);
		
		Element geoTagElement = doc.createElement("geoTag");
			
		doc.appendChild(geoTagElement);

		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/openStreetMapFiles.xsl\"");

		doc.insertBefore(xslRef, geoTagElement);

		XmlUtil.setChildText(geoTagElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(geoTagElement, "path", path, false);
		XmlUtil.setChildText(geoTagElement, "pathForScript", insertDoubleBackslash(path), false);
		XmlUtil.setChildText(geoTagElement, "shortPath", shortPath, false);
		
		processResponse("openStreetMapFiles.xsl");
    }
	
	/**
	 * We have to do the XSLT processing always on server side.
     * Client-side XSLT does not work in Firefox for Open Street Maps.
	 */
	public void processResponse(String xslFile)
    {
		String xslPath = WebFileSys.getInstance().getWebAppRootDir() + "xsl" + File.separator + xslFile;
    	
		TransformerFactory tf = TransformerFactory.newInstance();
	
		try
		{
			Transformer t =
					 tf.newTransformer(new StreamSource(new File(xslPath)));

			long start = System.currentTimeMillis();

			t.transform(new DOMSource(doc),
						new StreamResult(output));
	 		    
			long end = System.currentTimeMillis();
    
			if (Logger.getLogger(getClass()).isDebugEnabled()) {
				Logger.getLogger(getClass()).debug("XSLTC transformation in " + (end - start) + " ms");
			}
		}
		catch (TransformerConfigurationException tex)
		{
			Logger.getLogger(getClass()).warn(tex);
		}
		catch (TransformerException tex)
		{
			Logger.getLogger(getClass()).warn(tex);
		}

		output.flush();
    }
}