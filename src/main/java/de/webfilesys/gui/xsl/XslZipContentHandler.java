package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.IconManager;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslZipContentHandler extends XslRequestHandlerBase
{
    private DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");

	public XslZipContentHandler(
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
		String filePath = getParameter("filePath");

		if (!this.checkAccess(filePath))
		{
		    return;	
		}

		Element zipRootElement = doc.createElement("folderTree");
		
		doc.appendChild(zipRootElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/zipContent.xsl\"");

		doc.insertBefore(xslRef, zipRootElement);

		XmlUtil.setChildText(zipRootElement, "css", userMgr.getCSS(uid), false);

		addMsgResource("label.viewzip", getResource("label.viewzip","zip file content"));
		
		String shortFileName = this.getHeadlinePath(filePath);

		if (shortFileName.length() > 50)
		{
			shortFileName = shortFileName.substring(0,20) + "..." + shortFileName.substring(shortFileName.length()-27);
		}
		
        XmlUtil.setChildText(zipRootElement, "zipFilePath", filePath, false);
        XmlUtil.setChildText(zipRootElement, "zipFileEncodedPath", UTF8URLEncoder.encode(filePath), false);
		
		XmlUtil.setChildText(zipRootElement, "shortZipFileName", shortFileName, false);
		
		ZipFile zipFile = null;
		
		ZipEntry zipEntry = null;

		try
		{
			zipFile = new ZipFile(filePath);

			Enumeration entries = zipFile.entries();

			while (entries.hasMoreElements())
			{
				zipEntry = (ZipEntry) entries.nextElement();
				
				addZipPath(zipRootElement, zipEntry.getName(), zipEntry.isDirectory(),
	        			   zipEntry.getSize(),zipEntry.getCompressedSize());
			}

			zipFile.close();
		}
		catch (ZipException zipEx)
		{
			Logger.getLogger(getClass()).error("cannot open ZIP file: " + zipEx);
			return;
		}
		catch (IOException ioex)
		{
			Logger.getLogger(getClass()).error("cannot open ZIP file: " + ioex);
			return;
		}
		
		this.processResponse("zipContent.xsl", false);
	}
	
	private void addZipPath(Element zipRootElement, String zipEntryPath, boolean isDirectory,
            long entrySize, long compressedSize)
	{
        StringTokenizer pathParser = new StringTokenizer(zipEntryPath, File.separator + "/");		
        
        String path = "";
        
        Element folderElem = zipRootElement;
        
        while (pathParser.hasMoreTokens())
        {
        	String partOfPath = null;
        	
          	partOfPath = pathParser.nextToken();
        	
        	if ((File.separatorChar == '\\') && partOfPath.endsWith(":"))
        	{
        		partOfPath = partOfPath + "\\";
        	}
        	
        	if (path.length() == 0)
        	{
        		path = partOfPath;
        	}
        	else
        	{
        		if (path.endsWith(File.separator))
        		{
                	path = path + partOfPath;
        		}
        		else
        		{
                	path = path + File.separator + partOfPath;
        		}
        	}
        	
        	NodeList children = folderElem.getChildNodes();
        	
        	boolean nodeFound = false;
        	
        	Element subFolderElem = null;
        	
            int listLength = children.getLength();

            for (int i = 0; (!nodeFound) && (i < listLength); i++)
            {
                Node node = children.item(i);

                int nodeType = node.getNodeType();

                if (nodeType == Node.ELEMENT_NODE)
                {
                	subFolderElem = (Element) node;

                	if (subFolderElem.getTagName().equals("zipEntry"))
                	{
                		String subFolderName = subFolderElem.getAttribute("name");
                		
                		if (subFolderName.equals(partOfPath))
                		{
                			nodeFound = true;
                		}
                	}
                }
            }
            
            if (!nodeFound)
            {
            	String encodedPath = UTF8URLEncoder.encode(path);
            	
            	subFolderElem = doc.createElement("zipEntry");
            	
            	subFolderElem.setAttribute("name", partOfPath);
            	
				subFolderElem.setAttribute("path", encodedPath);   
				
				if (isDirectory || pathParser.hasMoreTokens()) 
				{
	                subFolderElem.setAttribute("folder", "true");   
				} 
				else 
				{
                    subFolderElem.setAttribute("file", "true");   
				}
				
				subFolderElem.setAttribute("entrySize", numFormat.format(entrySize));

				subFolderElem.setAttribute("compressedSize", numFormat.format(compressedSize));
				
				subFolderElem.setAttribute("icon" , getFileIcon(partOfPath));

                String lowerCasePartOfPath = partOfPath.toLowerCase();
            	
            	boolean stop = false;
            	
                for (int i = 0; (!stop) && (i < listLength); i++)
                {
                    Node node = children.item(i);
                    
                    int nodeType = node.getNodeType();

                    if (nodeType == Node.ELEMENT_NODE)
                    {
                    	Element existingElem = (Element) node;

                    	if (existingElem.getTagName().equals("zipEntry"))
                    	{
                    		String subFolderName = existingElem.getAttribute("name");
                    		
                    		if (subFolderName.toLowerCase().compareTo(lowerCasePartOfPath) > 0)
                    		{
                    			folderElem.insertBefore(subFolderElem, existingElem);
                    			
                    			stop = true;
                    		}
                    	}
                    }
                }
                    
                if (!stop)
                {
                	folderElem.appendChild(subFolderElem);
                }
            }
        	
            folderElem = subFolderElem;
        }
	}
	
	private String getFileIcon(String filePath)
	{
		int extIdx = filePath.lastIndexOf('.');

		if ((extIdx > 0) && (extIdx < (filePath.length() - 1)))
		{
			return(IconManager.getInstance().getAssignedIcon(filePath.substring(extIdx + 1)));
		}
		
		return(IconManager.DEFAULT_ICON);
	}

}