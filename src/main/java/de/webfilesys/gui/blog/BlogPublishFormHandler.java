package de.webfilesys.gui.blog;

import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.LanguageManager;
import de.webfilesys.gui.xsl.XslRequestHandlerBase;
import de.webfilesys.util.XmlUtil;

public class BlogPublishFormHandler extends XslRequestHandlerBase {
	
	public BlogPublishFormHandler(
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
		Element blogElement = doc.createElement("blog");
			
		doc.appendChild(blogElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/blog/publishBlog.xsl\"");

		doc.insertBefore(xslRef, blogElement);

		XmlUtil.setChildText(blogElement, "css", userMgr.getCSS(uid), false);
        
		Element languagesElement = doc.createElement("languages");
		
		blogElement.appendChild(languagesElement);
		
		Iterator<String> languages = LanguageManager.getInstance().getAvailableLanguages().iterator();
		
		while (languages.hasNext()) {
			Element languageElement = doc.createElement("language");
			XmlUtil.setElementText(languageElement, languages.next());
			languagesElement.appendChild(languageElement);
		}
		
		processResponse("blog/publishBlog.xsl", true);
    }

}
