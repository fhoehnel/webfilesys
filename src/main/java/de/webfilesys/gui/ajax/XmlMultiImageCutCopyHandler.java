package de.webfilesys.gui.ajax;

import java.io.File;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;

import de.webfilesys.ClipBoard;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XmlMultiImageCutCopyHandler extends XmlMultiImageRequestHandler
{
	public XmlMultiImageCutCopyHandler(
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
		if (!checkWriteAccess())
		{
			return;
		}
		
		ClipBoard clipBoard = (ClipBoard) session.getAttribute("clipBoard");
		
		// boolean clipBoardWasEmpty = ((clipBoard == null) || clipBoard.isEmpty());
		
		if (clipBoard != null)
		{
			clipBoard.reset();
		}
		else
		{
			clipBoard = new ClipBoard();
			
			session.setAttribute("clipBoard", clipBoard);
		}

        actPath = getCwd();
        
		String pathWithSlash = actPath;
		
		if (!actPath.endsWith(File.separator) && (!actPath.endsWith("/")))
		{
			pathWithSlash = actPath + File.separator;
		}
		
		for (int i = 0; i < selectedFiles.size(); i++)
		{
			String sourceFilename = pathWithSlash + selectedFiles.elementAt(i);
			clipBoard.addFile(sourceFilename);
		}

        if (cmd.equals("copy"))
        {
			clipBoard.setCopyOperation();
        }
        else
        {
			clipBoard.setMoveOperation();
        }
		
		String resultMsg = null;
		
		if (cmd.equals("copy"))
		{	
			resultMsg = selectedFiles.size() + " " + getResource("alert.filescopied","files copied to clipboard");
		}
		else
		{
			resultMsg = selectedFiles.size() + " " + getResource("alert.filesmoved","files moved to clipboard");
		}

		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "message", resultMsg);

		/*
        if (clipBoardWasEmpty)
        {
			XmlUtil.setChildText(resultElement, "clipboardWasEmpty", "true");
        }
        else
        {
			XmlUtil.setChildText(resultElement, "clipboardWasEmpty", "false");
        }
        */

		// do NOT reload the file list even if the clipboard was empty and the paste buttons are not showing
		XmlUtil.setChildText(resultElement, "clipboardWasEmpty", "false");

		XmlUtil.setChildText(resultElement, "success", "true");
			
		doc.appendChild(resultElement);
		
		this.processResponse();
	}
}
