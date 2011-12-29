package de.webfilesys.gui.xsl;

import java.io.File;
import java.io.PrintWriter;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.DirStat;
import de.webfilesys.FileSysStat;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 * @deprecated replaced by XslTreeStatsHandler
 */
public class XslTreeStatisticsHandler extends XslRequestHandlerBase
{
	public XslTreeStatisticsHandler(
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

		Element treeStatsElement = doc.createElement("treeStats");
			
		doc.appendChild(treeStatsElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/treeStats.xsl\"");

		doc.insertBefore(xslRef, treeStatsElement);

		XmlUtil.setChildText(treeStatsElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(treeStatsElement, "currentPath", currentPath, false);
		XmlUtil.setChildText(treeStatsElement, "relativePath", this.getHeadlinePath(currentPath), false);
		XmlUtil.setChildText(treeStatsElement, "shortPath", CommonUtils.shortName(this.getHeadlinePath(currentPath), 60), false);
		
		addMsgResource("label.subdirs", getResource("label.subdirs","subdirectories"));
		addMsgResource("label.subdirlevels", getResource("label.subdirlevels","subdir levels"));
		addMsgResource("label.firstlevelfiles", getResource("label.firstlevelfiles","files in first level"));
		addMsgResource("label.firstlevelbytes", getResource("label.firstlevelbytes","bytes in first level"));
		addMsgResource("label.treefiles", getResource("label.treefiles","files in tree"));
		addMsgResource("label.treebytes", getResource("label.treebytes","bytes in tree"));

		addMsgResource("button.closewin", getResource("button.closewin","Close Window"));

		FileSysStat fileSysStat = new FileSysStat(currentPath);

		Vector statList = fileSysStat.getStatistics();

		XmlUtil.setChildText(treeStatsElement, "subdirNum", CommonUtils.formatNumber(fileSysStat.getTotalSubdirNum(),14,false), false);

		XmlUtil.setChildText(treeStatsElement, "subdirLevels", Integer.toString(fileSysStat.getMaxLevel()), false);

		XmlUtil.setChildText(treeStatsElement, "dirFiles", CommonUtils.formatNumber(fileSysStat.getFirstLevelFileNum(),14,false), false);

		XmlUtil.setChildText(treeStatsElement, "dirBytes", CommonUtils.formatNumber(fileSysStat.getFirstLevelSizeSum(),18,false), false);

		XmlUtil.setChildText(treeStatsElement, "treeFiles", CommonUtils.formatNumber(fileSysStat.getTotalFileNum(),14,false), false);

		XmlUtil.setChildText(treeStatsElement, "treeBytes", CommonUtils.formatNumber(fileSysStat.getTotalSizeSum(),18,false), false);

		if (statList!=null)
		{
			for (int i = 0; i < statList.size(); i++)
			{
				DirStat dirStat = (DirStat) statList.elementAt(i);

				Element folderStatElement = doc.createElement("folderStat");

                treeStatsElement.appendChild(folderStatElement);

				String dirName = dirStat.getDirName();

				if (dirName.length() > 20)
				{
					dirName = dirName.substring(0,9) + "..." + dirName.substring(dirName.length() - 9);
				}

				XmlUtil.setChildText(folderStatElement, "folderName", dirName , false);

				String subDirPath = null;
				
				if (currentPath.endsWith(File.separator))
				{
					subDirPath = currentPath + dirStat.getDirName(); 
				}
				else
				{
					subDirPath = currentPath + File.separator + dirStat.getDirName(); 
				}

				XmlUtil.setChildText(folderStatElement, "path", UTF8URLEncoder.encode(subDirPath), false);

				XmlUtil.setChildText(folderStatElement, "byteNum", CommonUtils.formatNumber(dirStat.getTreeSize(), 18, false), false);

				long percent = 0L;

				if (fileSysStat.getMaxDirSize() > 0)
				{
					percent = (dirStat.getTreeSize() * 100) / fileSysStat.getMaxDirSize();
				}

				XmlUtil.setChildText(folderStatElement, "percent", Long.toString(percent), false);
			}
		}

		this.processResponse("treeStats.xsl", false);
    }
}