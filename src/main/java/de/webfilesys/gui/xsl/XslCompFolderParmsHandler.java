package de.webfilesys.gui.xsl;

import java.io.BufferedWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.gui.ajax.XmlSelectCompFolderHandler;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslCompFolderParmsHandler extends XslRequestHandlerBase
{
	public XslCompFolderParmsHandler(
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
		Element compFolderElement = doc.createElement("compareFolder");
			
		doc.appendChild(compFolderElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/compFolderParms.xsl\"");

		doc.insertBefore(xslRef, compFolderElement);
		
        String compSource = (String) session.getAttribute(XmlSelectCompFolderHandler.SESSION_ATTRIB_COMP_SOURCE);
        String compTarget = (String) session.getAttribute(XmlSelectCompFolderHandler.SESSION_ATTRIB_COMP_TARGET);

        XmlUtil.setChildText(compFolderElement, "sourcePath", CommonUtils.shortName(getHeadlinePath(compSource), 45), false);
        XmlUtil.setChildText(compFolderElement, "targetPath", CommonUtils.shortName(getHeadlinePath(compTarget), 45), false);
        
        addMsgResource("label.compSource", getResource("label.compSource", "compare folders"));
        addMsgResource("label.compSourceFolder", getResource("label.compSourceFolder", "source folder"));
        addMsgResource("label.compTargetFolder", getResource("label.compTargetFolder", "target folder"));
        addMsgResource("label.checkboxCompIgnoreDate", getResource("checkboxCompIgnoreDate", "ignore differences in modification date"));
        addMsgResource("checkboxCompShowAsTree", getResource("checkboxCompShowAsTree", "show differences as folder tree"));
        addMsgResource("checkboxCompIgnoreMetainf", getResource("checkboxCompIgnoreMetainf", "ignore WebFileSys metainf files"));
        addMsgResource("checkboxCompIgnorePattern", getResource("checkboxCompIgnorePattern", "ignore file matching this pattern:"));

        addMsgResource("button.compare", getResource("button.compare","start compare"));
		addMsgResource("button.cancel", getResource("button.cancel","Cancel"));

		resp.setContentType("text/xml");
		
		BufferedWriter xmlOutFile = new BufferedWriter(output);
            
		XmlUtil.writeToStream(doc, xmlOutFile);

		output.flush();
	}
}