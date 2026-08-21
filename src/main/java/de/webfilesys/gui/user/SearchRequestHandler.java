package de.webfilesys.gui.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;


import de.webfilesys.Category;
import de.webfilesys.Constants;
import de.webfilesys.FileLink;
import de.webfilesys.IconManager;
import de.webfilesys.LanguageManager;
import de.webfilesys.MetaInfManager;
import de.webfilesys.TextSearch;
import de.webfilesys.WebFileSys;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.PatternComparator;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * @author Frank Hoehnel
 */
public class SearchRequestHandler extends UserRequestHandler {
	MetaInfManager metaInfMgr = null;

	String searchResultDir = null;
	
	public SearchRequestHandler(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);

		metaInfMgr = MetaInfManager.getInstance();
	}

	protected void process() {
		String act_path = getParameter("actpath");
		
		if ((act_path == null) || (act_path.trim().length() == 0)) {
			act_path = getCwd();
		}

		if (!checkAccess(act_path)) {
			return;
		}

		String file_mask=getParameter("FindMask");
		if ((file_mask==null) || (file_mask.length()==0)) {
			file_mask="*";
		}

		
		StringBuilder searchTextBuff = new StringBuilder();
		String[] searchArguments = req.getParameterValues("searchText");
		
		for (int i = 0; i < searchArguments.length; i++) {
			if (!CommonUtils.isEmpty(searchArguments[i])) {
				searchTextBuff.append(searchArguments[i]);
				searchTextBuff.append(" ");
			}
		}
		String searchText = searchTextBuff.toString();

		String includeSubdirs = getParameter("includeSubdirs");
		String includeDesc=getParameter("includeDesc");
		String descOnly=getParameter("descOnly");

        Category category = null;

        String categoryName = getParameter("category");

        if (!categoryName.equals("-1")) {
        	category = new Category();
        	category.setName(categoryName);
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
		if (CommonUtils.isEmpty(searchText)) {
			output.print("<title>" + getResource("label.searchresults","Search Results") + ": " + file_mask + " </title>");
		} else {
			output.print("<title>" + getResource("label.searchresults","Search Results") + ": " + searchText + " </title>");
		}

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/common.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/skins/" + userMgr.getCSS(uid) + ".css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/icons.css\">");
		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/styles/fileIcons.css\">");

		output.println("<script src=\"/webfilesys/javascript/jquery/jquery.min.js\" type=\"text/javascript\"></script>");
		output.println("<script src=\"/webfilesys/javascript/ajaxCommon.js\" type=\"text/javascript\"></script>");
		output.println("<script src=\"/webfilesys/javascript/ajaxFolder.js\" type=\"text/javascript\"></script>");
		output.println("<script src=\"/webfilesys/javascript/util.js\" type=\"text/javascript\"></script>");
		output.println("<script src=\"/webfilesys/javascript/previewFile.js\" type=\"text/javascript\"></script>");

        output.println("<script language=\"javascript\">"); 

        output.println("window.resizeTo(700, 600);");

		String mobile = (String) session.getAttribute("mobile");
       	output.println("var mobile = " + Boolean.toString(mobile != null) + ";");
       	output.println("var searchResultDir = '" + UTF8URLEncoder.encode(searchResultDir) + "';");
        output.println("</script>"); 
        
        if (!readonly) {
    		output.println("<script src=\"/webfilesys/javascript/searchResult.js\" type=\"text/javascript\"></script>");
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

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.filemask","file mask") + ":");
		output.println("</td>");
		output.println("<td class=\"formParm2\">");
		output.println(file_mask);
		output.println("</td>");
		output.println("</tr>");

		if (!CommonUtils.isEmpty(searchText)) {
			output.println("<tr><td class=\"formParm1\">");
			output.println(getResource("label.searcharg","search argument") + ":");
			output.println("</td>");
			output.println("<td colspan=\"2\" class=\"formParm2\">");
			output.println(CommonUtils.escapeHTML(searchText));
			output.println("</td></tr>");
		}

		SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

		output.println("<tr><td class=\"formParm1\">");
		output.println(getResource("label.dateRange","modification date range") + ":");
		output.println("</td>");
		output.println("<td colspan=\"2\" class=\"formParm2\">");
		if (startDateProvided) {
			output.print(dateFormat.format(fromDate));
		}
		output.print("<b> ... </b>");
		output.println(dateFormat.format(toDate));
		output.println("</td></tr>");

        if (category != null) {
			output.println("<tr><td class=\"formParm1\">");
			output.println(getResource("label.assignedToCategory","assigned to category") + ":");
			output.println("</td>");
			output.println("<td colspan=\"2\" class=\"formParm2\">");
			output.println(category.getName());
			output.println("</td></tr>");
        }
        
		if (!CommonUtils.isEmpty(searchText)) {
			output.println("<tr><td class=\"formParm1\" colspan=\"3\">");
			output.println(getResource("label.currentSearchDir","searching in folder") + ":");
			output.println("</td></tr>");
			output.println("<tr><td class=\"formParm2\" colspan=\"3\">");
			output.println("&nbsp;");
			output.println("</td></tr>");
		}        

		output.println("</table>");

		output.flush();

        if (!readonly) {
			File searchResultDirFile = new File(searchResultDir);
        
			if (!searchResultDirFile.mkdirs()) {
				LogManager.getLogger(getClass()).error("cannot create search result directory " + searchResultDir);
			} else {
				StringBuffer searchArgText = new StringBuffer();

				searchArgText.append(getResource("label.searchresults","Search Results"));

				searchArgText.append(": \"");

				if (!CommonUtils.isEmpty(searchText)) {
					searchArgText.append(searchText);
					searchArgText.append("\" ");
					searchArgText.append(getResource("label.in","in"));
					searchArgText.append(" \"");
				}

				searchArgText.append(file_mask);
				searchArgText.append("\"");
            
				if (category != null) {
					searchArgText.append(" ");
					searchArgText.append(getResource("label.category","category"));
					searchArgText.append(" \"");
					searchArgText.append(category.getName());
					searchArgText.append("\"");
				}
        	
				metaInfMgr.setDescription(searchResultDir + File.separator + ".", searchArgText.toString());
			}
        }

        int hitNumber = 0;
        
		if (!CommonUtils.isEmpty(searchText)) {
			TextSearch textSearch = new TextSearch(act_path, file_mask, searchArguments, fromDate, toDate,
			                                       output, (includeSubdirs != null),
			                                       (includeDesc != null), (descOnly != null),
			                                       category, searchResultDir, session, readonly,
			                                       getHeadlinePath(act_path), uid);

			hitNumber = textSearch.getHitNumber();
		} else {
			hitNumber = findFiles(act_path, file_mask, (includeSubdirs != null), fromDate.getTime(), toDate.getTime(), category);
		}

		output.println("<table class=\"dataForm\" width=\"100%\" style=\"margin-top:10px\">");
		output.println("<tr>");
		output.println("<td class=\"fileListFunct\" style=\"padding:5px 10px\">");
		output.println(hitNumber + "  " + getResource("label.matches","matches found"));
		output.println("</td>");
		output.println("</tr>");
		
		output.println("<tr>");
		
        output.println("<td class=\"fileListFunct\">");		
        output.println("<div class=\"buttonCont\">");		
		
        if (readonly) {
			output.println("<input type=\"button\" value=\"" + getResource("button.closewin","Close Window") + "\" onClick=\"self.close()\">");
		} else {
			if (hitNumber > 0) {
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

		output.println("scrollTo(1, document.body.scrollHeight);");

		output.println("customAlert('" + hitNumber + " " + getResource("label.matches","matches found") + "', '" + getResource("button.ok","OK") + "', addPreviewHandler);");
		
		output.println("</script>");

		output.println("</body></html>");
		output.flush();
	}
	
	public int findFiles(String currentPath, String file_mask, boolean includeSubdirs, long fromDate, long toDate, Category category) {
		int searchHits = 0;
        if (currentPath.equals(searchResultDir)) {
            return searchHits;
        }

		if (session.getAttribute("searchCanceled") != null) {
			return searchHits;
		}

		boolean filePatternGiven = (!file_mask.equals("*")) && (!file_mask.equals("*.*"));

        File dirFile = new File(currentPath);
		File[] fileList = dirFile.listFiles();

		if (fileList != null) {
			for (File file : fileList) {
				if (file.isDirectory()) {
					if (includeSubdirs) {
						if (!dirIsLink(file)) {
							if (!file.getName().equals(ThumbnailThread.THUMBNAIL_SUBDIR)) {
								searchHits += findFiles(file.getAbsolutePath(), file_mask, includeSubdirs, fromDate, toDate, category);
							}
						}
					}
				} else {
					if (PatternComparator.patternMatch(file.getName(), file_mask)) {
						if (filePatternGiven || (!file.getName().equals(MetaInfManager.METAINF_FILE))) {
							// if any file with given date range is searched, ignore the metainf files
							
							if ((file.lastModified()>=fromDate) && (file.lastModified()<=toDate)) {
								if ((category == null) || metaInfMgr.isCategoryAssigned(currentPath, file.getName(), category)) {
									String viewLink = "/webfilesys/servlet?command=getFile&filePath=" + UTF8URLEncoder.encode(file.getAbsolutePath());

									String iconImg = IconManager.getInstance().getFileIconFont(file.getName());
									if (iconImg == null) {
										iconImg = "txt";
									}
									output.println("<div style=\"white-space:nowrap\">");
									output.println("<span class=\"icon-font fileIcon icon-file-" + iconImg + "\" style=\"margin-left:6px\"></span>");
									output.println("<a class=\"fn\" href=\"" + viewLink + "\" target=\"_blank\">" + getHeadlinePath(file.getAbsolutePath()) + "</a><br>");
									output.println("</div>");
									output.flush();

									searchHits++;

									if (!readonly) {
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
				}
			}
			if (category != null) {
                if (!currentPath.equals(searchResultDir)) {
                    metaInfMgr.releaseMetaInf(currentPath, false);
                }
            }
		} else {
			output.print("cannot get dir entries for " + currentPath + "<br>");
			output.flush();
		}
		return searchHits;
	}
}
