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
	int fileFindNum;

	MetaInfManager metaInfMgr = null;

	String searchResultDir = null;
	
	DecimalFormat distNumFormat = null;
	
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
	}

	protected void process() {
		String act_path = getParameter("actpath");
		
		if ((act_path == null) || (act_path.trim().length() == 0)) {
			act_path = getCwd();
		}

		if (!checkAccess(act_path)) {
			return;
		}

		String includeSubdirs = getParameter("includeSubdirs");
		
		String distanceParm = getParameter("distance");
		String latParm = getParameter("latitude");
		String longParm = getParameter("longitude");
		double distance = 10;
		double latitude = 0;
		double longitude = 0;
		try {
			distance = Double.parseDouble(distanceParm) * 1000;
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

		searchResultDir = act_path;

		if (!searchResultDir.endsWith(File.separator)) {
			searchResultDir = searchResultDir + File.separator;
		}
        
		searchResultDir = searchResultDir + Constants.SEARCH_RESULT_FOLDER_PREFIX + System.currentTimeMillis();

		output.print("<html>");
		output.print("<head>");
		output.print("<title>" + getResource("label.searchresults","Search Results") + ": GPS</title>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");

		output.println("<script src=\"/webfilesys/javascript/ajaxCommon.js\" type=\"text/javascript\"></script>");
		output.println("<script src=\"/webfilesys/javascript/ajaxFolder.js\" type=\"text/javascript\"></script>");
		output.println("<script src=\"/webfilesys/javascript/util.js\" type=\"text/javascript\"></script>");

        output.println("<script language=\"javascript\">"); 

        output.println("window.resizeTo(700, 600);");

		String mobile = (String) session.getAttribute("mobile");
       	output.println("var mobile = " + Boolean.toString(mobile != null) + ";");
       	output.println("var searchResultDir = '" + UTF8URLEncoder.encode(searchResultDir) + "';");
        output.println("</script>"); 
        
        if (!readonly)
        {
    		output.println("<script src=\"/webfilesys/javascript/search.js\" type=\"text/javascript\"></script>");
        }
		
		output.println("</head>");
		
		output.print("<body class=\"search searchText\">");

		headLine(getResource("label.searchresults","Search Results"));

		output.println("<br/>");
		
		output.println("<table class=\"dataForm\" width=\"100%\">");

        String relativePath = this.getHeadlinePath(act_path);

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.directory","directory") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println(CommonUtils.shortName(relativePath,40));
		output.println("</td>");
		output.println("<td rowspan=\"2\" class=\"formParm2\" valign=\"top\" align=\"right\">");
		output.println("<form accept-charset=\"utf-8\" name=\"form2\">");
		output.println("<input type=\"button\" name=\"cancelButton\" value=\"" + getResource("button.cancel","Cancel Search") + "\" onclick=\"cancelSearch()\">");
		output.println("</form>");
		output.println("</td>");
		output.println("</tr>");

		SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.dateRange","modification date range") + ":");
		output.println("</td>");
		output.println("<td colspan=\"2\" class=\"formParm2\">");
		if (startDateProvided)
		{
			output.print(dateFormat.format(fromDate));
		}
		output.print("<b> ... </b>");
		output.println(dateFormat.format(toDate));
		output.println("</td></tr>");
        
    	output.println("<tr><td class=\"formParm1\" colspan=\"3\">");
		output.println(getResource("label.currentSearchDir","searching in folder") + ":");
		output.println("</td></tr>");
		output.println("<tr><td class=\"formParm2\" colspan=\"3\">");
		output.println("&nbsp;");
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
				searchArgText.append(" distance=" + distance + " km");
				metaInfMgr.setDescription(searchResultDir + File.separator + ".", searchArgText.toString());
			}
        }

        int hitNumber = 0;
        
		fileFindNum = 0;
			
		searchTree(act_path, (includeSubdirs != null), fromDate.getTime(), toDate.getTime(), latitude, longitude, distance);

		hitNumber = fileFindNum;

		output.println("<table class=\"dataForm\" width=\"100%\" style=\"margin-top:10px\">");
		output.println("<tr>");
		output.println("<td class=\"fileListFunct\" style=\"padding:5px 10px\">");
		output.println(hitNumber + "  " + getResource("label.matches","matches found"));
		output.println("</td>");
		output.println("</tr>");
		
		output.println("<tr>");
		
        output.println("<td class=\"fileListFunct\">");		
        output.println("<div class=\"buttonCont\">");		
		
        if (readonly)		
		{
			output.println("<input type=\"button\" value=\"" + getResource("button.closewin","Close Window") + "\" onClick=\"self.close()\">");
		}
		else
		{
			if (hitNumber > 0)
			{		
				output.println("<input type=\"button\" value=\"" + getResource("button.keepSearchResults","Keep Search Results") + "\" onClick=\"showResults()\">");
			}
        
			output.println("<input type=\"button\" value=\"" + getResource("button.discardSearchResults","Discard Search Results") + "\" onClick=\"discardAndClose()\">");
		}

		output.println("</div>");
		output.println("</td>");

		output.println("</tr>");
		output.println("</table>");
		
		output.println("<script language=\"javascript\">");

		output.println("document.form2.cancelButton.style.visibility='hidden';");

		output.println("scrollTo(1,50000);");

		output.println("customAlert('" + hitNumber + " " + getResource("label.matches","matches found") + "', '" + getResource("button.ok","OK") + "');");
		
		output.println("</script>");

		output.println("</body></html>");
		output.flush();
	}
	
	public void searchTree(String currentPath, boolean includeSubdirs, long fromDate, long toDate, double latitude, double longitude, double distance) {
		LogManager.getLogger(getClass()).warn("searchTree currentPath=" + currentPath);
		
        if (currentPath.equals(searchResultDir)) {
            return;
        }
        
        File dirFile = new File(currentPath);
        String[] fileList = dirFile.list();

		if (fileList != null) {
			for (int i = 0; i < fileList.length; i++) {
                File tempFile = null;
                if (currentPath.endsWith(File.separator)) {
					tempFile = new File(currentPath + fileList[i]);
				} else {
					tempFile = new File(currentPath + File.separator + fileList[i]);
				}

        		LogManager.getLogger(getClass()).warn("searchTree current file=" + tempFile);
                
				if (tempFile.isDirectory()) {
					if (includeSubdirs) {
						if (!dirIsLink(tempFile)) {
							if (!fileList[i].equals(ThumbnailThread.THUMBNAIL_SUBDIR)) {
                                String subDir = null;
								if (currentPath.endsWith(File.separator)) {
									subDir = currentPath + fileList[i];
								} else {
									subDir = currentPath + File.separator + fileList[i];
								}
								searchTree(subDir, includeSubdirs, fromDate, toDate, latitude, longitude, distance);
							}
						}
					}
				} else {
					if (PatternComparator.patternMatch(fileList[i], "*.jpg") || PatternComparator.patternMatch(fileList[i], "*.jpeg")) {
						if ((tempFile.lastModified() >= fromDate) && (tempFile.lastModified() <= toDate)) {
							double locationDistance = locationInsideDistance(tempFile.getAbsolutePath(), latitude, longitude, distance);
							if (locationDistance >= 0) {
								String viewLink = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(tempFile.getAbsolutePath());
								
							    String iconImg = IconManager.getInstance().getIconForFileName(fileList[i]);
											
								output.print("<a class=\"fn\" href=\"" + viewLink + "\" target=\"_blank\"><img border=\"0\" src=\"icons/" + iconImg + "\" align=\"absbottom\"> " + getHeadlinePath(tempFile.getAbsolutePath()) + "</a>");
								output.print("<span class=\"searchMatchInContext\" style=\"margin-left:20px\">" + distNumFormat.format((locationDistance / 1000)) + " km</span>");
								output.println("<br/>");
								output.flush();
								fileFindNum++;
											
								try {
									metaInfMgr.createLink(searchResultDir, new FileLink(fileList[i], tempFile.getAbsolutePath(), uid));
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
		fileList = null;
	}

	private double locationInsideDistance(String filePath, double latitude, double longitude, double distance) {
		long startTime = System.currentTimeMillis();
		GeoTag geoTag = getGPSCoordinates(filePath);
		if (geoTag == null) {
			return -1;
		}
		LogManager.getLogger(getClass()).warn("GPS coordinates of " + filePath + ": " + geoTag.getLatitude() + ", " + geoTag.getLongitude() + "(" + (System.currentTimeMillis() - startTime) + " ms)");
		double locationDistance = calculateDistance(latitude, longitude, geoTag.getLatitude(), geoTag.getLongitude());
		LogManager.getLogger(getClass()).warn("distance of " + filePath + ": " + locationDistance);
		if (locationDistance <= distance) {
			return locationDistance;
		}
		return -1;
	}
	
	private GeoTag getGPSCoordinates(String filePath) {
		GeoTag geoTag = MetaInfManager.getInstance().getGeoTag(filePath);
		if (geoTag == null) {
			CameraExifData exifData = new CameraExifData(filePath);
			float latitude = exifData.getGpsLatitude();
			float longitude = exifData.getGpsLongitude();
			if (latitude < 0 || longitude < 0) {
				return null;
			}
			if ("S".equals(exifData.getGpsLatitudeRef())) {
				latitude = -latitude;
			}
			if ("W".equals(exifData.getGpsLongitudeRef())) {
				longitude = -longitude;
			}
			geoTag = new GeoTag();
			geoTag.setLatitude(latitude);
			geoTag.setLongitude(longitude);
		}
		return geoTag;
	}
	
    /**
     * Berechnet die Entfernung zwischen zwei Koordinaten in Metern.
     *
     * @param ax Breite der ersten Koordinate in Dezimalgrad
     * @param ay Laenge der ersten Koordinate in Dezimalgrad
     * @param bx Breite der zweiten Koordinate in Dezimalgrad
     * @param by Laenge der zweiten Koordinate in Dezimalgrad
     * @return Distanz in Metern 
     */
    double calculateDistance(double ax, double ay, double bx, double by) {
    
        if ((ax == bx) && (ay == by)) {
            return 0.0f;
        }
    
        double x = 1.0f / 298.257223563f;  // Abplattung der Erde
        
        double a = 6378137.0f / 1000.0f;  // Aequatorradius der Erde in km
        
        double f = (ax + bx) / 2.0f;
        
        double g = (ax - bx) / 2.0f;
        
        double l = (ay - by) / 2.0f;
        
        // auf Bogenmass bringen
        
        f = (Math.PI / 180.0f) * f;
        
        g = (Math.PI / 180.0f) * g;
        
        l = (Math.PI / 180.0f) * l;
        
        double s = Math.pow(Math.sin(g), 2) * Math.pow(Math.cos(l), 2) + Math.pow(Math.cos(f), 2) * Math.pow(Math.sin(l), 2);
        
        double c = Math.pow(Math.cos(g), 2) * Math.pow(Math.cos(l), 2) + Math.pow(Math.sin(f), 2) * Math.pow(Math.sin(l), 2);
        
        double w = Math.atan(Math.sqrt(s / c));

        double d = 2.0f * w * a;  
        
        double r = Math.sqrt(s * c) / w;
        
        double h1 = (3.0f * r - 1.0f) / (2.0f * c); 
        
        double h2 = (3.0f * r + 1.0f) / (2.0 * s); 
        
        return(1000.0f * d * (1.0f + x * h1 * Math.pow(Math.sin(f), 2) * Math.pow(Math.cos(g), 2) - x * h2 * Math.pow(Math.cos(f), 2) * Math.pow(Math.sin(g), 2))); 
    }
	
}
