package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.Constants;
import de.webfilesys.FileComparator;
import de.webfilesys.FileContainer;
import de.webfilesys.FileLinkSelector;
import de.webfilesys.FileSelectionStatus;
import de.webfilesys.IconManager;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.util.XmlUtil;

/**
 * Statistics (view/download count, voting count, comment count) for the files of the current
 * directory (Tab Statistics).
 * 
 * @author Frank Hoehnel
 */
public class XslFileListStatsHandler extends XslRequestHandlerBase
{
	public XslFileListStatsHandler(
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
		String currentPath = getParameter("actpath");

		if ((currentPath == null) || (currentPath.length() == 0))
		{
			currentPath = (String) session.getAttribute("cwd");
		}

		if (!accessAllowed(currentPath))
		{
			currentPath = userMgr.getDocumentRoot(uid);
		}
		
		session.setAttribute("cwd", currentPath);
		
		session.setAttribute("viewMode", new Integer(Constants.VIEW_MODE_STATS));

		int sortBy = FileComparator.SORT_BY_FILENAME;

        String temp = getParameter("sortBy");
        if ((temp != null) && (temp.length()>0))
        {
            try
            {
                sortBy = Integer.parseInt(temp);

                session.setAttribute("sortField", new Integer(sortBy));
            }
            catch (NumberFormatException nfe)
            {
            }
        }
        else
        {
            Integer sortField = (Integer) session.getAttribute("sortField");
            
            if (sortField != null)
            {
                sortBy = sortField.intValue();
            }
            else
            {
                sortBy = FileComparator.SORT_BY_FILENAME;
            }
        }
		
		Element fileListElem = doc.createElement("fileList");
			
		doc.appendChild(fileListElem);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/fileListStats.xsl\"");

		doc.insertBefore(xslRef, fileListElem);

		XmlUtil.setChildText(fileListElem, "css", userMgr.getCSS(uid), false);
	    XmlUtil.setChildText(fileListElem, "language", language, false);
		XmlUtil.setChildText(fileListElem, "currentPath", currentPath, false);
		XmlUtil.setChildText(fileListElem, "headLine", getHeadlinePath(currentPath), false);
		XmlUtil.setChildText(fileListElem, "sortBy", Integer.toString(sortBy), false);
		
        String description = MetaInfManager.getInstance().getDescription(currentPath, ".");
        if ((description != null) && (description.length() > 0))
        {
    		XmlUtil.setChildText(fileListElem, "description", description, true);
        }

		Date resetDate = MetaInfManager.getInstance().getStatisticsResetDate(currentPath);
		if (resetDate != null)
		{
			SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);
			XmlUtil.setChildText(fileListElem, "lastResetDate", dateFormat.format(resetDate), false);
		}
		
		String fileMasks[] = new String[1];
		fileMasks[0] = "*";

		FileLinkSelector fileSelector = new FileLinkSelector(currentPath, sortBy, true);

		FileSelectionStatus selectionStatus = fileSelector.selectFiles(fileMasks, Constants.MAX_FILE_NUM, 0);

		Vector selectedFiles = selectionStatus.getSelectedFiles();

		if (selectedFiles != null)
		{
			int fileNum = selectedFiles.size();
			
			for (int i = 0; i < fileNum; i++)
			{
				FileContainer fileCont = (FileContainer) selectedFiles.elementAt(i);
				
				Element fileElem = doc.createElement("file");
				
				fileListElem.appendChild(fileElem);
				
				String filename = fileCont.getName();

				File tempFile = fileCont.getRealFile();

				if (WebFileSys.getInstance().isShowAssignedIcons())
				{
					String docImage = null;

					int extIdx = filename.lastIndexOf('.');

					if ((extIdx > 0) && (extIdx < (filename.length() - 1)))
					{
						docImage = IconManager.getInstance().getAssignedIcon(filename.substring(extIdx + 1));
					}

					if (docImage == null) {
						docImage = "doc.gif";
					}
					
					fileElem.setAttribute("icon", docImage);
				}

                fileElem.setAttribute("name", filename);

                if (filename.length() > 50)
                {
                	String displayName = filename.substring(0, 45) + " " + filename.substring(45);
                    fileElem.setAttribute("displayName", displayName);
                }

                int viewCount = MetaInfManager.getInstance().getNumberOfDownloads(tempFile.getAbsolutePath());
                XmlUtil.setChildText(fileElem, "viewCount", Integer.toString(viewCount));

                int voteCount = MetaInfManager.getInstance().getVisitorRatingCount(tempFile.getAbsolutePath());
                XmlUtil.setChildText(fileElem, "voteCount", Integer.toString(voteCount));

                String realPath = tempFile.getParent();
                String realFileName = tempFile.getName();

				int commentCount = MetaInfManager.getInstance().countComments(realPath, realFileName);
                XmlUtil.setChildText(fileElem, "commentCount", Integer.toString(commentCount));

                if (commentCount > 0)
                {
                	XmlUtil.setChildText(fileElem, "pathForScript", insertDoubleBackslash(tempFile.getAbsolutePath()));
                }
			}
		}		
		
		processResponse("fileListStats.xsl", false);
    }
	
}