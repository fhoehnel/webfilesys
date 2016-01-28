package de.webfilesys.gui.ajax;

import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.EmojiManager;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlEmojiListHandler extends XmlRequestHandlerBase {
    
	public XmlEmojiListHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
		
		Element emojiListElem = doc.createElement("emojiList");
			
		doc.appendChild(emojiListElem);
		
		XmlUtil.setChildText(emojiListElem, "textareaId", req.getParameter("textareaId"));
			
		ArrayList<String> emojiList = EmojiManager.getInstance().getEmoticons();

        if (emojiList != null) {
        	for (String emoji : emojiList) {

			    Element emojiElem = doc.createElement("emoji");
					
			    emojiListElem.appendChild(emojiElem);
					
			    String emojiName = emoji;
			    if (emoji.lastIndexOf('.') > 0) {
			    	emojiName = emoji.substring(0, emoji.lastIndexOf('.'));
			    }
			    
				XmlUtil.setElementText(emojiElem, emojiName);
			}
        }
        
		processResponse();
    }
}