package de.webfilesys.viewhandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

import de.webfilesys.ViewHandlerConfig;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * Shows the content of a UNIX tar archive as a folder tree.
 * Uses the Java Tar Package written by Tim Endres.
 * 
 * @author Frank Hoehnel
 */
public class TarViewHandler implements ViewHandler {
	protected Document doc = null;

	protected DocumentBuilder builder = null;

	private DocumentBuilderFactory docFactory = null;

	Element resourcesElement = null;
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	
    DecimalFormat numFormat = new DecimalFormat("#,###,###,###,###");

	public void process(String filePath, ViewHandlerConfig viewHandlerConfig,
			HttpServletRequest req, HttpServletResponse resp) {
	    
	    TarInputStream tarFile = null;
	    
		try {
			docFactory = DocumentBuilderFactory.newInstance();

			builder = docFactory.newDocumentBuilder();

			doc = builder.newDocument();

			Element folderTreeElement = doc.createElement("folderTree");

			doc.appendChild(folderTreeElement);

			ProcessingInstruction xslRef = doc
					.createProcessingInstruction("xml-stylesheet",
							"type=\"text/xsl\" href=\"/webfilesys/xsl/tarContent.xsl\"");

			doc.insertBefore(xslRef, folderTreeElement);

			XmlUtil.setChildText(folderTreeElement, "css", "fmweb", false);

			addMsgResource("label.viewzip", "view tar archive content");

			String fileName = filePath;
			if (filePath.indexOf(File.separatorChar) > 0) {
				fileName = filePath.substring(filePath
						.lastIndexOf(File.separatorChar) + 1);
			} else if (filePath.indexOf('/') > 0) {
				fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
			}

			XmlUtil.setChildText(folderTreeElement, "shortZipFileName", fileName);

			tarFile = new TarInputStream(new FileInputStream(filePath));

			TarEntry tarEntry = null;

			while ((tarEntry = tarFile.getNextEntry()) != null) {
		        addTarPath(folderTreeElement, tarEntry.getName(),
		        		tarEntry.getSize(), tarEntry.getModTime());
			}

			PrintWriter output = resp.getWriter();

			resp.setContentType("text/xml");

			BufferedWriter xmlOutFile = new BufferedWriter(output);

			XmlUtil.writeToStream(doc, xmlOutFile);

			output.flush();
			
			tarFile.close();
		} catch (ParserConfigurationException pcex) {
			LogManager.getLogger(getClass()).error(pcex.toString());
		} catch (FileNotFoundException e) {
			LogManager.getLogger(getClass()).error(
					"failed to extract content of tar archive", e);

			return;
		} catch (IOException e) {
			LogManager.getLogger(getClass()).error(
					"failed to extract content of tar archive", e);

			if (tarFile != null) {
			    try {
	                tarFile.close();                
			    } catch (Exception ex) {
			    }
			}
			
			return;
		}
	}

	private void addTarPath(Element zipRootElement, String zipEntryPath,
			long entrySize, Date lastModified) {
		StringTokenizer pathParser = new StringTokenizer(zipEntryPath,
				File.separator + "/");

		String path = "";

		Element folderElem = zipRootElement;

		while (pathParser.hasMoreTokens()) {
			String partOfPath = null;

			partOfPath = pathParser.nextToken();

			if ((File.separatorChar == '\\') && partOfPath.endsWith(":")) {
				partOfPath = partOfPath + "\\";
			}

			if (path.length() == 0) {
				path = partOfPath;
			} else {
				if (path.endsWith(File.separator)) {
					path = path + partOfPath;
				} else {
					path = path + File.separator + partOfPath;
				}
			}

			NodeList children = folderElem.getChildNodes();

			boolean nodeFound = false;

			Element subFolderElem = null;

			int listLength = children.getLength();

			for (int i = 0; (!nodeFound) && (i < listLength); i++) {
				Node node = children.item(i);

				int nodeType = node.getNodeType();

				if (nodeType == Node.ELEMENT_NODE) {
					subFolderElem = (Element) node;

					if (subFolderElem.getTagName().equals("folder")) {
						String subFolderName = subFolderElem
								.getAttribute("name");

						if (subFolderName.equals(partOfPath)) {
							nodeFound = true;
						}
					}
				}
			}

			if (!nodeFound) {
				String encodedPath = UTF8URLEncoder.encode(path);

				subFolderElem = doc.createElement("folder");

				subFolderElem.setAttribute("name", partOfPath);

				subFolderElem.setAttribute("path", encodedPath);

				subFolderElem.setAttribute("entrySize", numFormat.format(entrySize));

				subFolderElem.setAttribute("lastModified", dateFormat.format(lastModified));

				subFolderElem.setAttribute("icon", "doc.gif");

				String lowerCasePartOfPath = partOfPath.toLowerCase();

				boolean stop = false;

				for (int i = 0; (!stop) && (i < listLength); i++) {
					Node node = children.item(i);

					int nodeType = node.getNodeType();

					if (nodeType == Node.ELEMENT_NODE) {
						Element existingElem = (Element) node;

						if (existingElem.getTagName().equals("folder")) {
							String subFolderName = existingElem
									.getAttribute("name");

							if (subFolderName.toLowerCase().compareTo(
									lowerCasePartOfPath) > 0) {
								folderElem.insertBefore(subFolderElem,
										existingElem);

								stop = true;
							}
						}
					}
				}

				if (!stop) {
					folderElem.appendChild(subFolderElem);
				}
			}

			folderElem = subFolderElem;
		}
	}

	protected void addMsgResource(String key, String value) {
		if (resourcesElement == null) {
			resourcesElement = doc.createElement("resources");

			doc.getDocumentElement().appendChild(resourcesElement);
		}

		Element msgElement = doc.createElement("msg");

		resourcesElement.appendChild(msgElement);

		msgElement.setAttribute("key", key);
		msgElement.setAttribute("value", value);
	}
    
    /**
     * Create the HTML response for viewing the given file contained in a ZIP archive..
     * 
     * @param zipFilePath path of the ZIP entry
     * @param zipIn the InputStream for the file extracted from a ZIP archive
     * @param req the servlet request
     * @param resp the servlet response
     */
    public void processZipContent(String zipFilePath, InputStream zipIn, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req, HttpServletResponse resp)
    {
        // not yet supported
        LogManager.getLogger(getClass()).warn("reading from ZIP archive not supported by ViewHaandler " + this.getClass().getName());
    }
    
    /**
     * Does this ViewHandler support reading the file from an input stream of a ZIP archive?
     * @return true if reading from ZIP archive is supported, otherwise false
     */
    public boolean supportsZipContent()
    {
        return false;
    }
}