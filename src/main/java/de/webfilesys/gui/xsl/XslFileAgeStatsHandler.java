package de.webfilesys.gui.xsl;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.stats.AgeCategory;
import de.webfilesys.stats.DirStatsByAge;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslFileAgeStatsHandler extends XslRequestHandlerBase
{
    private static final long MILLIS_DAY = 24L * 60L * 60L * 1000L;
    private static final long MILLIS_WEEK = 7L * MILLIS_DAY;
    private static final long MILLIS_YEAR = 365L * MILLIS_DAY;
    
    
	public XslFileAgeStatsHandler(
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

		DirStatsByAge ageStats = new DirStatsByAge();
		
        ageStats.addAgeCategory(new AgeCategory(0L, "< 1 " + getResource("stats.age.day", "day")));
        ageStats.addAgeCategory(new AgeCategory(MILLIS_DAY, "1 " + getResource("stats.age.day", "day")));
        ageStats.addAgeCategory(new AgeCategory(2L * MILLIS_DAY, "2 " + getResource("stats.age.days", "days")));
        ageStats.addAgeCategory(new AgeCategory(MILLIS_WEEK, "1 " + getResource("stats.age.week", "week")));
        ageStats.addAgeCategory(new AgeCategory(4L * MILLIS_WEEK, "4 " + getResource("stats.age.weeks", "weeks")));
        ageStats.addAgeCategory(new AgeCategory(MILLIS_YEAR / 2L, "1/2 " + getResource("stats.age.year", "year")));
        ageStats.addAgeCategory(new AgeCategory(MILLIS_YEAR, "1 " + getResource("stats.age.year", "year")));
        ageStats.addAgeCategory(new AgeCategory(3L * MILLIS_YEAR, "3 " + getResource("stats.age.years", "years")));
        ageStats.addAgeCategory(new AgeCategory(10L * MILLIS_YEAR, "10 " + getResource("stats.age.years", "years")));
		
        ageStats.determineStatistics(currentPath);
        
		ArrayList statisticResults = ageStats.getResults();
		
		Element treeStatsElement = doc.createElement("treeStats");
			
		doc.appendChild(treeStatsElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/fileAgeTreeStats.xsl\"");

		doc.insertBefore(xslRef, treeStatsElement);

		XmlUtil.setChildText(treeStatsElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(treeStatsElement, "currentPath", currentPath, false);
		XmlUtil.setChildText(treeStatsElement, "relativePath", this.getHeadlinePath(currentPath), false);
		XmlUtil.setChildText(treeStatsElement, "shortPath", CommonUtils.shortName(this.getHeadlinePath(currentPath), 60), false);
		
        addMsgResource("stats.ageWinTitle", getResource("stats.ageWinTitle","Directory Statistics by File Age"));
        addMsgResource("stats.fileAge", getResource("stats.fileAge","file age"));
        addMsgResource("stats.fileNum", getResource("stats.fileNum","# files"));
        addMsgResource("stats.percentFileNum", getResource("stats.percentFileNum","% file num"));
        addMsgResource("stats.sizeSum", getResource("stats.sizeSum","size sum"));
        addMsgResource("stats.percentSizeSum", getResource("stats.percentSizeSum","% size"));

		addMsgResource("button.closewin", getResource("button.closewin","Close Window"));

		Element sizeStatsElem = doc.createElement("ageStats");
        treeStatsElement.appendChild(sizeStatsElem);
        
        DecimalFormat numFormat = new DecimalFormat("#,###,###,###");

        long fileNumCategoryMax = ageStats.getFileNumCategoryMax();
        
        long sizeSumCategoryMax = ageStats.getSizeSumCategoryMax();
        
        for (int i = 0; i < statisticResults.size(); i++) {
            AgeCategory ageCat = (AgeCategory) statisticResults.get(i);

            Element clusterElem = doc.createElement("cluster");
            XmlUtil.setChildText(clusterElem, "fileAge", ageCat.getDisplayText());
            XmlUtil.setChildText(clusterElem, "fileNum", numFormat.format(ageCat.getFileNum()));
            XmlUtil.setChildText(clusterElem, "sizeSum", numFormat.format(ageCat.getSizeSum()));
            XmlUtil.setChildText(clusterElem, "numberPercent", Integer.toString(ageCat.getFileNumPercent()));
            XmlUtil.setChildText(clusterElem, "sizePercent", Integer.toString(ageCat.getSizePercent()));
            XmlUtil.setChildText(clusterElem, "fileNumPercentOfMax", Long.toString((ageCat.getFileNum() * 100L) / fileNumCategoryMax));
            XmlUtil.setChildText(clusterElem, "sizeSumPercentOfMax", Long.toString((ageCat.getSizeSum() * 100L) / sizeSumCategoryMax));
            sizeStatsElem.appendChild(clusterElem);
        }
		
		this.processResponse("fileAgeTreeStats.xsl", false);
    }
	
}	
