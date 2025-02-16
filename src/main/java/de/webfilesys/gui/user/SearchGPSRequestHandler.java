package de.webfilesys.gui.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.util.GPSUtil;
import org.apache.logging.log4j.LogManager;

import de.webfilesys.Constants;
import de.webfilesys.FileLink;
import de.webfilesys.GeoTag;
import de.webfilesys.IconManager;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.graphics.CameraExifData;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.PatternComparator;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class SearchGPSRequestHandler extends UserRequestHandler {
	MetaInfManager metaInfMgr = null;

	DecimalFormat distNumFormat = null;
	DecimalFormat coordFormat = null;

	public SearchGPSRequestHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);

		metaInfMgr = MetaInfManager.getInstance();
		distNumFormat = new DecimalFormat();
		distNumFormat.setMaximumFractionDigits(3);
		coordFormat = new DecimalFormat();
		coordFormat.setMaximumFractionDigits(6);
	}

	protected void process() {
		String currentPath = getParameter("actpath");

		if ((currentPath == null) || (currentPath.trim().length() == 0)) {
			currentPath = getCwd();
		}

		if (!checkAccess(currentPath)) {
			return;
		}

		String includeSubdirs = getParameter("includeSubdirs");
		
		String distanceParm = getParameter("distance");
		String latParm = getParameter("latitude");
		String longParm = getParameter("longitude");
		double searchDistance = 10;
		double latitude = 0;
		double longitude = 0;
		try {
			searchDistance = Double.parseDouble(distanceParm) * 1000;
			latitude = Double.parseDouble(latParm);
			longitude = Double.parseDouble(longParm);
		} catch (Exception ex) {
			LogManager.getLogger(getClass()).warn("invalid distance value " + distanceParm, ex);
		}

		String datePickerFormat = getResource("datePickerFormat", "mm/dd/yy").replace("m",  "M");
        
        SimpleDateFormat dateParser = new SimpleDateFormat(datePickerFormat);
        
		Date fromDate = new Date(0L);
		Date toDate = new Date();

		String dateRangeFrom = getParameter("dateRangeFrom");
		String dateRangeUntil = getParameter("dateRangeUntil");
		
		boolean startDateProvided = false;
		
		try {
			if (!CommonUtils.isEmpty(dateRangeFrom)) {
				fromDate = dateParser.parse(dateRangeFrom);
				fromDate.setHours(0);
				fromDate.setMinutes(0);
				fromDate.setSeconds(0);
				startDateProvided = true;
			}
			if (!CommonUtils.isEmpty(dateRangeUntil)) {
				toDate = dateParser.parse(dateRangeUntil);
                toDate.setHours(23);
                toDate.setMinutes(59);
                toDate.setSeconds(59);
			}
		} catch (Exception ex) {
			LogManager.getLogger(getClass()).warn("invalid date format in search date range", ex);
		}

		session.removeAttribute("searchCanceled");

		String searchResultDir = currentPath;

		if (!searchResultDir.endsWith(File.separator)) {
			searchResultDir = searchResultDir + File.separator;
		}
        
		searchResultDir = searchResultDir + Constants.SEARCH_RESULT_FOLDER_PREFIX + System.currentTimeMillis();

		output.print("<html>");
		output.print("<head>");
		output.print("<title>" + getResource("label.searchresults","Search Results") + ": GPS</title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/icons.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/fileIcons.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("<script src=\"/webfilesys/javascript/jquery/jquery.min.js\" type=\"text/javascript\"></script>");
		output.println("<script src=\"/webfilesys/javascript/ajaxCommon.js\" type=\"text/javascript\"></script>");
		output.println("<script src=\"/webfilesys/javascript/ajaxFolder.js\" type=\"text/javascript\"></script>");
		output.println("<script src=\"/webfilesys/javascript/searchResult.js\" type=\"text/javascript\"></script>");
		output.println("<script src=\"/webfilesys/javascript/util.js\" type=\"text/javascript\"></script>");
		output.println("<script src=\"/webfilesys/javascript/previewFile.js\" type=\"text/javascript\"></script>");

		output.println("<script language=\"javascript\">");

        output.println("window.resizeTo(700, 600);");

		String mobile = (String) session.getAttribute("mobile");
       	output.println("var mobile = " + Boolean.toString(mobile != null) + ";");
       	output.println("var searchResultDir = '" + UTF8URLEncoder.encode(searchResultDir) + "';");
        output.println("</script>"); 
        
		output.println("</head>");
		
		output.print("<body class=\"search searchText\">");

		headLine(getResource("label.searchresults","Search Results"));

		output.println("<br/>");
		
		output.println("<table class=\"dataForm\" width=\"100%\">");

        String relativePath = this.getHeadlinePath(currentPath);

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.directory","directory") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println(CommonUtils.shortName(relativePath,40));
		output.println("</td>");
		output.println("</tr>");

		SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.dateRange","modification date range") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		if (startDateProvided)
		{
			output.print(dateFormat.format(fromDate));
		}
		output.print("<b> ... </b>");
		output.println(dateFormat.format(toDate));
		output.println("</td></tr>");

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.latitudeSearchResult","latitude"));
		output.print(", ");
		output.println(getResource("label.longitudeSearchResult","longtitude"));
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.print(coordFormat.format(latitude) + "&nbsp;&nbsp;" + coordFormat.format(longitude));
		output.println("</td></tr>");

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.searchGPSDistance","distance"));
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.print(distNumFormat.format(searchDistance));
		output.println("</td></tr>");

		output.println("<tr id=\"cancelButtonCont\">");
		output.println("<td colspan=\"2\" class=\"formParm2\" style=\"text-align:right\">");
		output.println("<form accept-charset=\"utf-8\" name=\"form2\">");
		output.println("<input type=\"button\" name=\"cancelButton\" value=\"" + getResource("button.cancel","Cancel Search") + "\" onclick=\"cancelSearch()\">");
		output.println("</form>");
		output.println("</tr>");

    	output.println("<tr id=\"currentSearchDirLabelCont\"><td class=\"formParm1\" colspan=\"2\">");
		output.println(getResource("label.currentSearchDir","searching in folder") + ":");
		output.println("</td></tr>");
		output.println("<tr id=\"currentSearchDirCont\"><td class=\"formParm2\" colspan=\"2\">");
		output.println("<span id=\"currentSearchDir\"></span>");
		output.println("</td></tr>");

		output.println("</table>");

		output.flush();

        if (!readonly) {
			File searchResultDirFile = new File(searchResultDir);
        
			if (!searchResultDirFile.mkdirs()) {
				LogManager.getLogger(getClass()).error("cannot create search result directory " + searchResultDir);
			} else {
				StringBuffer searchArgText = new StringBuffer();
				searchArgText.append(getResource("label.searchresults","Search Results"));
				searchArgText.append(": GPS");
				searchArgText.append(" latitude=" + latitude);
				searchArgText.append(" longitude=" + longitude);
				searchArgText.append(" distance=" + searchDistance + " km");
				metaInfMgr.setDescription(searchResultDir + File.separator + ".", searchArgText.toString());
			}
        }

		output.println("<ul id=\"searchResultList\" class=\"searchResultList\"></ul>");

		output.println("<table class=\"dataForm\" width=\"100%\" style=\"margin-top:10px\">");
		output.println("<tr>");
		output.println("<td id=\"matchCount\" class=\"fileListFunct\" style=\"padding:5px 10px\">");
		output.println("0 " + getResource("label.matches","matches found"));
		output.println("</td>");
		output.println("</tr>");
		
		output.println("<tr>");
		
        output.println("<td class=\"fileListFunct\">");		
        output.println("<div class=\"buttonCont\">");

		if (readonly) {
			output.println("<input id=\"closeButton\" type=\"button\" style=\"visibility:hidden\" value=\"" + getResource("button.closewin", "Close Window") + "\" onClick=\"self.close()\">");
		} else {
			output.println("<input id=\"keepButton\" type=\"button\" style=\"visibility:hidden\" value=\"" + getResource("button.keepSearchResults","Keep Search Results") + "\" onClick=\"showResults()\">");
			output.println("<input id=\"discardButton\" type=\"button\" style=\"visibility:hidden\" value=\"" + getResource("button.discardSearchResults","Discard Search Results") + "\" onClick=\"discardSearchResults()\">");
		}

		output.println("</div>");
		output.println("</td>");

		output.println("</tr>");
		output.println("</table>");

		output.println("</body>");
		output.flush();

		int matchCount = searchTree(currentPath, (includeSubdirs != null), searchResultDir, fromDate.getTime(), toDate.getTime(), latitude, longitude, searchDistance);

		output.println("<script>");

		output.println("document.getElementById(\"matchCount\").innerHTML = \"" + matchCount + " " + getResource("label.matches","matches found") + "\";");

		output.println("document.getElementById(\"cancelButtonCont\").style.display = \"none\";");
		output.println("document.getElementById(\"currentSearchDirLabelCont\").style.display = \"none\";");
		output.println("document.getElementById(\"currentSearchDirCont\").style.display = \"none\";");

		if (readonly) {
			output.println("document.getElementById(\"closeButton\").style.visibility = \"visible\";");
		} else {
			if (matchCount > 0) {
				output.println("document.getElementById(\"keepButton\").style.visibility = \"visible\";");
			}
			output.println("document.getElementById(\"discardButton\").style.visibility = \"visible\";");
		}

		output.println("scrollTo(1,100000);");

		output.println("customAlert('" + matchCount + " " + getResource("label.matches","matches found") + "', '" + getResource("button.ok","OK") + "', addPreviewHandler);");

		output.println("</script>");
		output.println("</html>");
		output.flush();
	}
	
	public int searchTree(String currentPath, boolean includeSubdirs, String searchResultDir,
						  long fromDate, long toDate, double latitude, double longitude, double distance) {
		int searchHits = 0;
        if (currentPath.equals(searchResultDir)) {
            return searchHits;
        }

		if (session.getAttribute("searchCanceled") != null) {
			return searchHits;
		}

		output.println("<script>document.getElementById(\"currentSearchDir\").innerHTML = \"" + CommonUtils.escapeForJavascript(CommonUtils.shortName(currentPath, 80))+ "\";</script>");
		output.flush();

        File dirFile = new File(currentPath);
        File[] fileList = dirFile.listFiles();

		if (fileList != null) {
			for (File file : fileList) {
				if (file.isDirectory()) {
					if (includeSubdirs) {
						if (!dirIsLink(file)) {
							if (!file.getName().equals(ThumbnailThread.THUMBNAIL_SUBDIR)) {
								searchHits += searchTree(file.getAbsolutePath(), includeSubdirs, searchResultDir, fromDate, toDate, latitude, longitude, distance);
							}
						}
					}
				} else {
					if (PatternComparator.patternMatch(file.getName(), "*.jpg") || PatternComparator.patternMatch(file.getName(), "*.jpeg")) {
						double locationDistance = getDistance(file.getAbsolutePath(), latitude, longitude);
						if (locationDistance >= 0 && locationDistance <= distance) {
							long fileDate = getExposureOrModificationaTime(file);
							if (fileDate >= fromDate && fileDate <= toDate) {
								String viewLink = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(file.getAbsolutePath());

								String iconImg = IconManager.getInstance().getIconForFileName(file.getName());

								String searchresultPath = CommonUtils.escapeForJavascript(getHeadlinePath(file.getAbsolutePath()));
								String viewLinkForScript = CommonUtils.escapeForJavascript(viewLink);
								String formattedDistance = distNumFormat.format(locationDistance / 1000);

								output.println("<script>");
								output.println("appendSearchResult(\"" + searchresultPath + "\", \"" + viewLinkForScript + "\", \"" + iconImg + "\", \"" + formattedDistance + "\");");
								output.println("</script>");

								output.flush();
								searchHits++;

								try {
									metaInfMgr.createLink(searchResultDir, new FileLink(file.getName(), file.getAbsolutePath(), uid));
								} catch (FileNotFoundException nfex) {
									LogManager.getLogger(getClass()).error(nfex);
								}
							}
						}
					}
				}
			}
		} else {
			output.print("cannot get dir entries for " + currentPath + "<br>");
			output.flush();
		}
		return searchHits;
	}

	private long getExposureOrModificationaTime(File file) {
		CameraExifData exifData = new CameraExifData(file.getAbsolutePath());
		if (exifData != null) {
			Date exposureDate = exifData.getExposureDate();
			if (exposureDate != null) {
				return exposureDate.getTime();
			}
		}
		return file.lastModified();
	}

	private double getDistance(String filePath, double latitude, double longitude) {
		long startTime = System.currentTimeMillis();
		GeoTag geoTag = GPSUtil.getGPSCoordinates(filePath);
		if (geoTag == null) {
			return -1;
		}
		LogManager.getLogger(getClass()).warn("GPS coordinates of " + filePath + ": " + geoTag.getLatitude() + ", " + geoTag.getLongitude() + "(" + (System.currentTimeMillis() - startTime) + " ms)");
		double locationDistance = GPSUtil.calculateDistance(latitude, longitude, geoTag.getLatitude(), geoTag.getLongitude());
		LogManager.getLogger(getClass()).warn("distance of " + filePath + ": " + locationDistance);
		return locationDistance;
	}
	
}
