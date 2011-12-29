package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.stats.DirStatsBySize;
import de.webfilesys.stats.SizeCategory;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslFileSizeStatsHandler extends XslRequestHandlerBase
{
    private static final long KB = 1024;
    private static final long MB = 1024 * KB;
    private static final long GB = 1024 * MB;
    
	public XslFileSizeStatsHandler(
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

		DirStatsBySize dirStats = new DirStatsBySize(currentPath);
		
		ArrayList statisticResults = dirStats.getResults();
		
		Element treeStatsElement = doc.createElement("treeStats");
			
		doc.appendChild(treeStatsElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/fileSizeTreeStats.xsl\"");

		doc.insertBefore(xslRef, treeStatsElement);

		XmlUtil.setChildText(treeStatsElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(treeStatsElement, "currentPath", currentPath, false);
		XmlUtil.setChildText(treeStatsElement, "relativePath", this.getHeadlinePath(currentPath), false);
		XmlUtil.setChildText(treeStatsElement, "shortPath", CommonUtils.shortName(this.getHeadlinePath(currentPath), 60), false);
		
        addMsgResource("stats.sizeWinTitle", getResource("stats.sizeWinTitle","Directory Statistics by File Size"));
        addMsgResource("stats.fileSize", getResource("stats.fileSize","file size"));
        addMsgResource("stats.fileNum", getResource("stats.fileNum","# files"));
        addMsgResource("stats.percentFileNum", getResource("stats.percentFileNum","% file num"));
        addMsgResource("stats.sizeSum", getResource("stats.sizeSum","size sum"));
        addMsgResource("stats.percentSizeSum", getResource("stats.percentSizeSum","% size"));

		addMsgResource("button.closewin", getResource("button.closewin","Close Window"));

		Element sizeStatsElem = doc.createElement("sizeStats");
        treeStatsElement.appendChild(sizeStatsElem);
        
        DecimalFormat numFormat = new DecimalFormat("#,###,###,###");

        long fileNumCategoryMax = dirStats.getFileNumCategoryMax();
        
        long sizeSumCategoryMax = dirStats.getSizeSumCategoryMax();
        
        for (int i = 0; i < statisticResults.size(); i++) {
            SizeCategory sizeCat = (SizeCategory) statisticResults.get(i);

            Element clusterElem = doc.createElement("cluster");
            XmlUtil.setChildText(clusterElem, "minSize", formatSize(sizeCat.getMinSize()));
            XmlUtil.setChildText(clusterElem, "maxSize", formatSize(sizeCat.getMaxSize()));
            XmlUtil.setChildText(clusterElem, "fileNum", numFormat.format(sizeCat.getFileNum()));
            XmlUtil.setChildText(clusterElem, "sizeSum", numFormat.format(sizeCat.getSizeSum()));
            XmlUtil.setChildText(clusterElem, "numberPercent", Integer.toString(sizeCat.getFileNumPercent()));
            XmlUtil.setChildText(clusterElem, "sizePercent", Integer.toString(sizeCat.getSizePercent()));
            XmlUtil.setChildText(clusterElem, "fileNumPercentOfMax", Long.toString((sizeCat.getFileNum() * 100L) / fileNumCategoryMax));
            XmlUtil.setChildText(clusterElem, "sizeSumPercentOfMax", Long.toString((sizeCat.getSizeSum() * 100L) / sizeSumCategoryMax));
            sizeStatsElem.appendChild(clusterElem);
        }
		
		this.processResponse("fileSizeTreeStats.xsl", false);
    }
	
    private String formatSize(long sizeVal) 
    {
        StringBuffer formattedSize = new StringBuffer();
        
        long formatVal = sizeVal;
        
        if (formatVal >= GB) 
        {
            formattedSize.append(formatVal / GB);
            formattedSize.append(".");
            formatVal = formatVal % GB;
            formattedSize.append(formatVal / MB);
            formattedSize.append(" GB");
        } 
        else 
        {
            if (formatVal >= MB) 
            {
                formattedSize.append(formatVal / MB);
                formattedSize.append(".");
                formatVal = formatVal % MB;
                formattedSize.append(formatVal / KB);
                formattedSize.append(" MB");
            } 
            else 
            {
                if (formatVal >= KB) 
                {
                    formattedSize.append(formatVal / KB);
                    formattedSize.append(".");
                    formatVal = formatVal % KB;
                    formattedSize.append(formatVal);
                    formattedSize.append(" KB");
                } 
                else
                {
                    formattedSize.append(formatVal);
                    formattedSize.append(" Byte");
                }
            }
        }
        
        return formattedSize.toString();
    }
}