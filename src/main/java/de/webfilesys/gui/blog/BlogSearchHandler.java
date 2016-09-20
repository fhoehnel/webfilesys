package de.webfilesys.gui.blog;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.Comment;
import de.webfilesys.MetaInfManager;
import de.webfilesys.gui.ajax.XmlRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

/**
 * Search the blog text and comments.
 * @author Frank Hoehnel
 */
public class BlogSearchHandler extends XmlRequestHandlerBase {
	
	private static final int BEFORE_CONTEXT_LENGTH = 30;
	private static final int AFTER_CONTEXT_LENGTH = 30;
	
	private static final int MAX_HITS_PER_FILE = 5;
	
	public BlogSearchHandler(
    		HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid) {
        super(req, resp, session, output, uid);
	}
	
	protected void process() {
		String searchArg = getParameter("searchArg");
		
		if (CommonUtils.isEmpty(searchArg)) {
            Logger.getLogger(getClass()).error("missing parameter searchArg");
            return;
		}

		String searchCommentsParam = getParameter("searchComments");
		
		boolean searchComments = (searchCommentsParam != null) && (!searchCommentsParam.isEmpty());
		
        Element resultElem = doc.createElement("result");
        
		XmlUtil.setChildText(resultElem, "success", "true");
		
		doc.appendChild(resultElem);
		
        Element searchResultsElem = doc.createElement("searchResults");
        
        resultElem.appendChild(searchResultsElem);
        
        XmlUtil.setChildText(searchResultsElem, "searchArg", searchArg);

        int hitCount = 0;
        
        TreeMap<String, ArrayList<SearchResultData>> searchResultMap = searchBlog(searchArg, searchComments);
        
        for (String dateKey : searchResultMap.keySet()) {
        	
        	ArrayList<SearchResultData> dayResultList = searchResultMap.get(dateKey);
            
        	if (dayResultList.size() > 0) {
            	Element blogDayElem = doc.createElement("blogDay");
                XmlUtil.setChildText(blogDayElem, "linkDate", dateKey);
                
                XmlUtil.setChildText(blogDayElem, "displayDate", formatBlogDate(dayResultList.get(0).getDisplayDate()));

                searchResultsElem.appendChild(blogDayElem);

            	for (SearchResultData searchHit : dayResultList) {
                	Element searchHitElement = doc.createElement("searchHit");
                	
                	XmlUtil.setChildText(searchHitElement, "matchingText", searchHit.getMatchingText());

                	if (searchHit.getBeforeContext() != null) {
                    	XmlUtil.setChildText(searchHitElement, "beforeContext", searchHit.getBeforeContext());
                	}
                	
                	if (searchHit.getAfterContext() != null) {
                    	XmlUtil.setChildText(searchHitElement, "afterContext", searchHit.getAfterContext());
                	}
                	
                	XmlUtil.setChildText(searchHitElement, "fileName", searchHit.getFileName());
                	
                	if (searchHit.getIsComment()) {
                    	XmlUtil.setChildText(searchHitElement, "isComment", "true");
                	}

                	blogDayElem.appendChild(searchHitElement);
                	
                	hitCount++;
                }
        	}
        }

        XmlUtil.setChildText(searchResultsElem, "hitCount", Integer.toString(hitCount));
        
		processResponse();
	}
	
	private TreeMap<String, ArrayList<SearchResultData>> searchBlog(String searchArg, boolean searchComments) {
		TreeMap<String, ArrayList<SearchResultData>> resultMap = new TreeMap<String, ArrayList<SearchResultData>>();

		String currentPath = userMgr.getDocumentRoot(uid).replace('/',  File.separatorChar);

		MetaInfManager metaInfMgr = MetaInfManager.getInstance();
		
		boolean stagedPublication = metaInfMgr.isStagedPublication(currentPath);
		
		File blogFolder = new File(currentPath);
		
		File[] blogFiles = blogFolder.listFiles();
		
		SimpleDateFormat linkDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		for (int i = 0; i < blogFiles.length; i++) {
			if (blogFiles[i].isFile()) {
				
            	if ((!readonly) || (!stagedPublication) || (metaInfMgr.getStatus(blogFiles[i].getAbsolutePath()) != MetaInfManager.STATUS_BLOG_EDIT)) {
            		
    				String blogText = metaInfMgr.getDescription(currentPath, blogFiles[i].getName());
    				if (blogText != null) {
    					ArrayList<SearchResultData> hitList = searchInBlogText(blogText, searchArg);
    					
    					if (searchComments) {
    						ArrayList<SearchResultData> commentHitList = searchInComments(currentPath, blogFiles[i].getName(), searchArg);
    						if (commentHitList.size() > 0) {
    							hitList.addAll(commentHitList);
    						}
    					}
    					
    					String blogDateStr = blogFiles[i].getName().substring(0,10);
    					
    					Date blogDate;
    					try {
    						blogDate = linkDateFormat.parse(blogDateStr);

    						Date linkDate = new Date(blogDate.getTime() + (24l * 60l * 60l * 1000l));
    						
    					    String linkDateStr = linkDateFormat.format(linkDate);
    					    
    						for (SearchResultData searchHit : hitList) {
    							searchHit.setLinkDate(linkDateStr); 
    							searchHit.setDisplayDate(blogDate);
    							searchHit.setFileName(blogFiles[i].getName());
    						}
    						
    						ArrayList<SearchResultData> existingList = resultMap.get(linkDateStr);
    						if (existingList != null) {
    							existingList.addAll(hitList);
    						} else {
    							resultMap.put(linkDateStr, hitList);
    						}
    					} catch (Exception ex) {
    			            Logger.getLogger(getClass()).error("invalid blog date format: " + blogDateStr , ex);
    					}
    				}
            	}				
			}
		}
		return resultMap;
	}

	ArrayList<SearchResultData> searchInBlogText(String blogText, String searchArg) {
		
		ArrayList<SearchResultData> resultList = new ArrayList<SearchResultData>();
		
		String lowerCaseBlogText = blogText.toLowerCase();
		String lowerCaseSearchArg = searchArg.toLowerCase();
		
		int idx = 0;
        int hitIdx = 0; 
        int hitCount = 0;
		
		do {
			hitIdx = lowerCaseBlogText.indexOf(lowerCaseSearchArg, idx);
			if (hitIdx >= 0) {
				SearchResultData searchResult = new SearchResultData();
				searchResult.setMatchingText(blogText.substring(hitIdx, hitIdx + searchArg.length()));
                if (hitIdx > 0) {
                	int beforeContextStart = hitIdx - BEFORE_CONTEXT_LENGTH;
                	if (beforeContextStart < 0) {
                		beforeContextStart = 0;
                	}
                	searchResult.setBeforeContext(blogText.substring(beforeContextStart, hitIdx));
                }
                
                if (hitIdx < lowerCaseBlogText.length() - lowerCaseSearchArg.length() -1) {
                    int afterContextEnd = hitIdx +  lowerCaseSearchArg.length() + AFTER_CONTEXT_LENGTH;
                    if (afterContextEnd > lowerCaseBlogText.length()) {
                    	afterContextEnd = lowerCaseBlogText.length();
                    }
                	searchResult.setAfterContext(blogText.substring(hitIdx + searchArg.length(), afterContextEnd));
                }
				
                resultList.add(searchResult);                
                
				idx = hitIdx + lowerCaseSearchArg.length();
				
				hitCount++;
			}
		} while ((hitIdx >= 0) && (idx < lowerCaseBlogText.length() - lowerCaseSearchArg.length()) && (hitCount < MAX_HITS_PER_FILE));
		
		return resultList;
	}
	
	public ArrayList<SearchResultData> searchInComments(String currentPath, String fileName, String searchArg) {
		ArrayList<SearchResultData> resultList = new ArrayList<SearchResultData>();

		Vector<Comment> comments = MetaInfManager.getInstance().getListOfComments(currentPath, fileName);
		if (comments != null) {
			for (Comment comment : comments) {
				if (comment.getMessage() != null) {
					ArrayList<SearchResultData> matchesInComment = searchInBlogText(comment.getMessage(), searchArg);
					if (matchesInComment.size() > 0) {
						for (SearchResultData matchInComment : matchesInComment) {
							matchInComment.setIsComment(true);
						}
						resultList.addAll(matchesInComment);
					}
				}
			}
		}
		return resultList;
	}
	
	public class SearchResultData {
		private String beforeContext;
		private String afterContext;
		private String matchingText;
		private String linkDate;
		private Date displayDate;
		private String fileName;
		private boolean isComment;
		
		public void setBeforeContext(String newVal) {
			beforeContext = newVal;
		}
		
		public String getBeforeContext() {
			return beforeContext;
		}
		
		public void setAfterContext(String newVal) {
			afterContext = newVal;
		}
		
		public String getAfterContext() {
			return afterContext;
		}
		
		public void setLinkDate(String newVal) {
			linkDate = newVal;
		}
		
		public String getLinkDate() {
			return linkDate;
		}

		public void setDisplayDate(Date newVal) {
			displayDate = newVal;
		}
		
		public Date getDisplayDate() {
			return displayDate;
		}
		
		public void setMatchingText(String newVal) {
			matchingText = newVal;
		}
		
		public String getMatchingText() {
			return matchingText;
		}
		
		public void setFileName(String newVal) {
			fileName = newVal;
		}
		
		public String getFileName() {
			return fileName;
		}
		
		public void setIsComment(boolean newVal) {
			isComment = newVal;
		}
		
		public boolean getIsComment() {
			return isComment;
		}
	}
}
