package de.webfilesys.gui.xsl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.webfilesys.WebFileSys;
import de.webfilesys.gui.user.UserRequestHandler;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 *
 */
public class XslRequestHandlerBase extends UserRequestHandler
{
	private static final Logger LOG = Logger.getLogger(XslRequestHandlerBase.class);
	
	protected Document doc;

	private DocumentBuilder builder;

	Element resourcesElement = null;

	Element requestParmsElement = null;
	
	Element validationElement = null;
	
	HttpServletResponse resp = null;
	
	public XslRequestHandlerBase(
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
			LOG.error("failed to build XML DOM doc", pcex);
		}
	}

    public Document getXmlDoc()
    {
    	return(doc);
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

	protected void addRequestParameter(String key, String value)
	{
		if (requestParmsElement == null)
		{
			requestParmsElement = doc.createElement("requestParms");
			
			doc.getDocumentElement().appendChild(requestParmsElement);
		}
		
		Element requestParmElement = doc.createElement("requestParm");
		
		requestParmsElement.appendChild(requestParmElement);
		
		requestParmElement.setAttribute("key", key);
		
		XmlUtil.setElementText(requestParmElement, value, true);
	}

	protected void addRequestParameter(String key)
	{
		String value = req.getParameter(key);
		
		if (value == null)
		{
			return;
		}
		
		if (requestParmsElement == null)
		{
			requestParmsElement = doc.createElement("requestParms");
			
			doc.getDocumentElement().appendChild(requestParmsElement);
		}
		
		Element requestParmElement = doc.createElement("requestParm");
		
		requestParmsElement.appendChild(requestParmElement);
		
		requestParmElement.setAttribute("key", key);
		
		XmlUtil.setElementText(requestParmElement, value, true);
	}
	
	/**
	 * Validation error messages
	 * @param field the name of the form field
	 * @param message the validation error message
	 */
	protected void addValidationError(String field, String message)
	{
		if (validationElement == null)
		{
			validationElement = doc.createElement("validation");
			
			// the validation element is not automatically appended to the root element 
			// because the root element might not exist at this time
			// doc.getDocumentElement().appendChild(validationElement);
		}
		
		Element errorElement = doc.createElement("error");
		
		validationElement.appendChild(errorElement);
		
		errorElement.setAttribute("field", field);
		
		errorElement.setAttribute("message", message);
	}
	
	/**
	 *  path for picture album
	 */
    protected void addAlbumPath(String actPath, Element rootElement,
                                Element currentPathElem)
    {
		XmlUtil.setChildText(rootElement, "role", "album");
        	
		String docRoot = userMgr.getDocumentRoot(uid);

		String relativePath = actPath.substring(docRoot.length());
			
		Element partOfPathElem = doc.createElement("pathElem");
			
		currentPathElem.appendChild(partOfPathElem);
			
		partOfPathElem.setAttribute("name", uid);
			
		partOfPathElem.setAttribute("path", File.separator);
		
		StringTokenizer pathParser = new StringTokenizer(relativePath, File.separator);
		
		StringBuffer partialPath = new StringBuffer();
		
		while (pathParser.hasMoreTokens())
		{
			String partOfPath = pathParser.nextToken();
			
			partialPath.append(partOfPath);
			
			if (pathParser.hasMoreTokens())
			{
				partialPath.append(File.separatorChar);		
			}
			
			partOfPathElem = doc.createElement("pathElem");
			
			currentPathElem.appendChild(partOfPathElem);
			
			partOfPathElem.setAttribute("name", partOfPath);
			
			partOfPathElem.setAttribute("path", UTF8URLEncoder.encode(partialPath.toString()));
		}
    }

	public void processResponse(String xslFile)
	{
		processResponse(xslFile, false);
	}

    /**
     * This problem seems to be history with a current version of firefox:
     * Mozilla browsers (Firefox, Netscape) have a very strange bug in the XSLT processor.
     * HTML select boxes generated from XSL do not work in most cases, the selected value cannot
     * be changed and the onchange() event handler is not called.
     * 
     * If the resulting HTML page contains a HTML select form element and the client is Mozilla,
     * we have to do the XSL processing on the server.
     * 
     * @param xslFile the name of the XSL file
     * @param handleMozillaXslBug
     */
	public void processResponse(String xslFile, boolean handleMozillaXslBug)
    {
		if ((session != null) && isBrowserXslEnabled())
		    // && ((!handleMozillaXslBug) || (browserManufacturer != BROWSER_MOZILLA)))
		{
			// Logger.getLogger(getClass()).debug("client-side XSLT: " + xslFile);
			
			resp.setContentType("text/xml");

			BufferedWriter xmlOutFile = new BufferedWriter(output);
                
			XmlUtil.writeToStream(doc, xmlOutFile);
		}
        else
        { 
    		resp.setContentType("text/html");
        	
			String xslPath = WebFileSys.getInstance().getWebAppRootDir() + "xsl" + File.separator + xslFile;
        	
        	// Logger.getLogger(getClass()).debug("server-side XSLT: " + xslPath);

			TransformerFactory tf = TransformerFactory.newInstance();
		
			try
			{
				Transformer transformer =
						 tf.newTransformer(new StreamSource(new File(xslPath)));

				transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				
				long start = System.currentTimeMillis();

				transformer.transform(new DOMSource(doc), new StreamResult(output));
		 		    
				long end = System.currentTimeMillis();
        
				if (LOG.isDebugEnabled()) 
				{
					LOG.debug("server-side XSL transformation in " + (end - start) + " ms");
				}
			}
			catch (TransformerConfigurationException tex)
			{
				LOG.warn(tex);
			}
			catch (TransformerException tex)
			{
				LOG.warn(tex);
			}
        }

		output.flush();
    }

}
