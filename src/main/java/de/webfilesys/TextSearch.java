package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import de.webfilesys.decoration.Decoration;
import de.webfilesys.decoration.DecorationManager;
import de.webfilesys.graphics.ThumbnailThread;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.PatternComparator;

public class TextSearch
{
    PrintWriter output;
    int hitNum;

    HttpSession session;
    
    boolean readonly = false;
    
    String searchResultDir = null;
    
    private String uid = null;

    boolean includeSubdirs;
    boolean includeMetaInf;
    boolean metaInfOnly;
    
    Category category = null;

    MetaInfManager metaInfMgr=null;

    String[] searchArgs;
    
    int dirCounter = 0;
    
    public TextSearch(String root_dir, String file_mask, String[] searchArguments, Date fromDate, Date toDate,
               PrintWriter output, boolean includeSubdirs, boolean includeMetaInf, boolean metaInfOnly,
               Category category, String searchResultDir,
               HttpSession session, boolean readonly,
               String relativePath,
               String uid)
    {
        this.output=output;
        this.searchResultDir = searchResultDir;
        this.session=session;
        this.readonly = readonly;
        this.uid = uid;

        hitNum = 0;

        metaInfMgr = MetaInfManager.getInstance();

        this.includeSubdirs = includeSubdirs;
        this.includeMetaInf=includeMetaInf;
        this.metaInfOnly=metaInfOnly;
        
        this.category = category;

        searchArgs = filterEmptySearchArguments(searchArguments);

        search_tree(root_dir,file_mask,fromDate.getTime(),toDate.getTime(), relativePath);

        if (hitNum > 0) {
        	Decoration deco = new Decoration();
        	deco.setIcon("search.gif");
        	deco.setTextColor("#808080");
        	DecorationManager.getInstance().setDecoration(searchResultDir, deco);
        }
        
        output.flush();
    }

    String[] filterEmptySearchArguments(String[] searchArguments) {
    	ArrayList<String> filteredList = new ArrayList<String>();
    	for (int i = 0; i < searchArguments.length; i++) {
    		if (!CommonUtils.isEmpty(searchArguments[i])) {
    			filteredList.add(searchArguments[i]);
    		}
    	}
    	return filteredList.toArray(new String[0]);
    }
    
    public int getHitNumber() {
        return(hitNum);
    }

    public void search_tree(String act_path,String file_mask, long fromDate, long toDate,
                            String relativePath) {
        if (session.getAttribute("searchCanceled")!=null) {
            return;
        }

        String shortPath = CommonUtils.shortName(relativePath, 70);
        
        if (session.getAttribute("searchCanceled") == null) {
        	dirCounter++;
        	
        	boolean printPath = true;
        	if (dirCounter > 10000) {
        		printPath = (dirCounter % 50 == 0);
        	} else if (dirCounter > 5000) {
        		printPath = (dirCounter % 20 == 0);
        	} else if (dirCounter > 1000) {
        		printPath = (dirCounter % 10 == 0);
        	} else if (dirCounter > 500) {
        		printPath = (dirCounter % 5 == 0);
        	} else if (dirCounter > 100) {
        		printPath = (dirCounter % 3 == 0);
        	}
        	
        	if (printPath) {
    			output.println("<div class=\"searchPath\"><span class=\"searchPath\">" + shortPath + "</span></div>");
                output.flush();
        	}
        }

        File dirFile = new File(act_path);
        File[] fileList = dirFile.listFiles();

        if (fileList != null) {
            for (int i = 0; (session.getAttribute("searchCanceled") == null) && (i < fileList.length); i++) {
                String relativeFile = null;

                if (act_path.endsWith(File.separator)) {
                    relativeFile = relativePath + fileList[i].getName();
                } else {
					relativeFile = relativePath + File.separator + fileList[i].getName();
                }
                
                File tempFile = fileList[i];

            	String fullPath = tempFile.getAbsolutePath();
            	
                if (tempFile.isDirectory()) {
                	if (includeMetaInf) {
                		// search in metainfo of folder

                		if (!tempFile.getName().startsWith(Constants.SEARCH_RESULT_FOLDER_PREFIX)) {
                    		String description = metaInfMgr.getDescription(fullPath, ".");

    						if (description != null) {
    							boolean allWordsFound = true;
    							for (int j = 0; allWordsFound && (j < searchArgs.length); j++) {
    								if (description.toLowerCase().indexOf(searchArgs[j].toLowerCase()) < 0) {
    								    allWordsFound = false;
    								}
    							}
    							if (allWordsFound) {
                                	String folderViewLink = "javascript:gotoSearchResultFolder('" + CommonUtils.escapeForJavascript(fullPath) + "')";

    								output.println("<a class=\"fn\" href=\"" + folderViewLink + "\"><img border=\"0\" src=\"/webfilesys/images/folder.gif\" style=\"margin-top:8px;\"> " + relativeFile + "</a>");
    								output.println("<br/>");
    								output.flush();
    								hitNum++;
    							}
    						}
                		}
                	}
                	
                	if (includeSubdirs) {
                        if (!CommonUtils.dirIsLink(tempFile)) {
    						if (!fileList[i].getName().equals(ThumbnailThread.THUMBNAIL_SUBDIR)) {
    							String relativeSubPath = null;
    							
    					        String subDir;
    							
    							if (act_path.endsWith(File.separator)) {
    								subDir = act_path + fileList[i].getName();
    								
    								relativeSubPath = relativePath + fileList[i].getName();
    							} else {
    								subDir = act_path + File.separator + fileList[i].getName();

    								relativeSubPath = relativePath + File.separator + fileList[i].getName();
    							}
    							
    							search_tree(subDir, file_mask, fromDate, toDate, relativeSubPath);
    						}
                        }
                	}
                } else {                                             // file
                    if ((tempFile.lastModified() >= fromDate) &&
                        (tempFile.lastModified() <= toDate))
                    {
                        if (PatternComparator.patternMatch(fileList[i].getName(), file_mask))
                        {
                        	if ((category == null) || metaInfMgr.isCategoryAssigned(fullPath, category))
                        	{
								boolean allWordsFound = true;

								int firstMatchIdx[] = new int[searchArgs.length];

								for (int j = 0; (j < searchArgs.length) && allWordsFound; j++) {
									firstMatchIdx[j] = (-1);

									if (metaInfOnly) {
										if (!searchInMetaInf(fullPath, searchArgs[j])) {
											allWordsFound = false;
										}
									} else {
										if (metaInfMgr.isMetaInfFile(fullPath)) {
											allWordsFound = false;
										} else {
											firstMatchIdx[j]=locateTextInFile(tempFile.toString(), searchArgs[j]);
                                        
											if (firstMatchIdx[j] < 0) {
												if (!includeMetaInf) {
													allWordsFound = false;
												} else {
													if (!searchInMetaInf(fullPath, searchArgs[j])) {
														allWordsFound = false;
													}
												}
											}
										}
									}
								}
                            
								if (allWordsFound) {
                                    String viewLink = null;
                                    try {
                                        viewLink = "/webfilesys/servlet?command=getFile&filePath=" + URLEncoder.encode(fullPath, "UTF-8");
                                    } catch (UnsupportedEncodingException uex) {
                                        // should never happen
                                    }

									if (session.getAttribute("searchCanceled")==null) {
                                        String iconImg = "doc.gif";

                                        if (WebFileSys.getInstance().isShowAssignedIcons()) {
                                            iconImg = IconManager.getInstance().getIconForFileName(fileList[i].getName());
                                        }
									    
										output.println("<a class=\"fn\" href=\"" + viewLink + "\" target=\"_blank\"><img border=\"0\" src=\"icons/" + iconImg + "\" align=\"absbottom\" style=\"margin-top:8px;\"> " + relativeFile + "</a>");
										output.println("<br/>");
										output.flush();
										hitNum++;

										printMatches(fullPath,firstMatchIdx);
										
										if (!readonly) {
											try {
												metaInfMgr.createLink(searchResultDir, new FileLink(fileList[i].getName(), fullPath, uid), true);
											} catch (FileNotFoundException nfex) {
												Logger.getLogger(getClass()).error(nfex);
											}
										}
									}
								}
                        	}
                        }
                    }
                }
            }

            if ((category != null) || includeMetaInf || metaInfOnly) {
                if (!act_path.equals(searchResultDir)) {
                    metaInfMgr.releaseMetaInf(act_path, false);
                }
            }
        } else {
            output.println("cannot get dir entries for " + act_path);
            output.println("<br/>");
            output.flush();
        }
        
        fileList=null;
    }

    public int locateTextInFile(String act_file,String search_arg)
    {
        int search_length = search_arg.length();

        FileInputStream file_input = null;

        try
        {
            file_input = new FileInputStream(act_file);
        }
        catch (FileNotFoundException e)
        {
            Logger.getLogger(getClass()).error("cannot open search result file", e);
            return(-1);
        }

        int idx=0;

        int count=0;

        int equal=0;

        try
        {
            byte[] buffer = new byte[4096];

            while (( count = file_input.read(buffer))>=0 )
            {
                for (int i = 0; i < count; i++)
                {
                    if (Character.toUpperCase((char) buffer[i])==Character.toUpperCase(search_arg.charAt(equal)))
                    {
                        if (++equal==search_length)
                        {
                            return(idx - search_arg.length() + 1);
                        }
                    }
                    else
                    {
                        equal=0;
                    }
                    
                    idx++;
                }
            }
        }
        catch (IOException e)
        {
        	Logger.getLogger(getClass()).warn("fulltext search error: " + e);
        }
        finally
        {
        	if (file_input != null)
        	{
        		try
        		{
        			file_input.close();
        		}
        		catch (Exception ex) 
        		{
        		}
        	}
        }

        return(-1);
    }

    protected void printMatches(String fileName,int firstMatchIdx[])
    {
        for (int i = 0; i < searchArgs.length; i++)
        {
            if (firstMatchIdx[i] > 0)
            {
                printHitEnvironment(fileName, searchArgs[i], firstMatchIdx[i]);
            } else {
            	if (includeMetaInf || metaInfOnly) {
            		printMatchingDescription(fileName, searchArgs[i]);
            		printMatchingTags(fileName, searchArgs[i]);
            	}
            }
        }
    }

    protected void printMatchingDescription(String filePath, String searchArg) {
    	String description = metaInfMgr.getDescription(filePath);
    	if (!CommonUtils.isEmpty(description)) {
    		int matchIdx = description.toLowerCase().indexOf(searchArg.toLowerCase());
    		if (matchIdx >= 0) {
        		int prefixStartIdx = matchIdx - 10;
        		if (prefixStartIdx < 0) {
        			prefixStartIdx = 0;
        		}
        		int postFixEndIdx = matchIdx + searchArg.length() + 10;
        		if (postFixEndIdx > description.length() - 1) {
        			postFixEndIdx = description.length() - 1;
        		}
        		
        		output.print("<span class=\"plaintext\" style=\"margin-left:30px;\">description: ");             		

        		if (prefixStartIdx < matchIdx) {
        			output.print("..." + description.substring(prefixStartIdx, matchIdx));
        		}
        		output.print("<b>");
        		output.print(searchArg);
        		output.print("</b>");
        		if (postFixEndIdx >= matchIdx + searchArg.length()) {
        			output.print(description.substring(matchIdx + searchArg.length(), postFixEndIdx) + "...");
        		}
        		
        		output.println("</span>");
                output.println("<br/>");
    		}
    	}
    }
    
    protected void printMatchingTags(String filePath, String searchArg) {
    	ArrayList<String> tags = metaInfMgr.getTags(filePath);
        if (tags != null) {
        	boolean anyTagMatches = false;
        	for (String tag : tags) {
        		if (tag.toLowerCase().indexOf(searchArg.toLowerCase()) >= 0) {
            		output.print("<span class=\"plaintext\" style=\"margin-left:30px;\">tag: ");             		
                    output.print(tag);
            		output.println("</span>");
            		anyTagMatches = true;
        		}
        	}
        	if (anyTagMatches) {
                output.println("<br/>");
        	}
        }
    }
    
    protected void printHitEnvironment(String fileName,String searched,int firstMatchIdx)
    {
        int searchLength = searched.length();

        char resultBuff[] = new char[searchLength + 10]; 

        for (int i = 0; i < resultBuff.length; i++)
        {
            resultBuff[i] = ' ';
        }
        
        int matchCounter = 0;

        int count=0;

        int equal=0;

        FileInputStream fin = null;

        try
        {
            fin = new FileInputStream(fileName);
        	
            int startIdx = firstMatchIdx - 10;

            if (startIdx < 0)
            {
                startIdx = 0;
            }
            
            if (startIdx > 0)
            {
            	if (fin.skip(startIdx) != startIdx) 
            	{
                    Logger.getLogger(getClass()).warn("cannot locate to search hit index " + firstMatchIdx);
            	}
            }

            byte[] buffer = new byte[4096];

            while ((matchCounter < 5) && ((count = fin.read(buffer))>=0))
            {
                for (int i=0;(matchCounter < 5) && (i<count);i++)
                {
                    char ch=(char) buffer[i];

                    for (int k=0;k<resultBuff.length-1;k++)
                    {
                        resultBuff[k]=resultBuff[k+1];
                    }
                    
                    if ((ch=='\n') || (ch=='\r'))
                    {
                        resultBuff[resultBuff.length-1]=' ';
                    }
                    else
                    {
                        resultBuff[resultBuff.length-1]=ch;
                    }

                    if (Character.toUpperCase(ch) == Character.toUpperCase(searched.charAt(equal)))
                    {
                        if (++equal == searchLength)
                        {
                            if (matchCounter == 0)
                            {
                                output.println("<span class=\"plaintext\" style=\"margin-left:30px;\">");
                            }

                            output.print("<b>...</b> ");

                            String prefix = new String(resultBuff,0,resultBuff.length-searchLength);

                            output.print(CommonUtils.escapeHTML(prefix));

                            output.print("<span class=\"searchMatchInContext\">");

                            String matchText = new String(resultBuff, resultBuff.length-searchLength, searchLength);
                            output.print(CommonUtils.escapeHTML(matchText));

                            output.print("</span>");

                            StringBuffer postfix = new StringBuffer();
                            
                            for (int t = i + 1; (t < count) && (t < i + 11); t++)
                            {
                                char ch2 = (char) buffer[t];
                                
                                if ((ch2=='\n') || (ch2=='\r'))
                                {
                                    postfix.append(' ');
                                }
                                else
                                {
                                    postfix.append(ch2);
                                }
                            }
                            
                            output.print(CommonUtils.escapeHTML(postfix.toString()));
                            
                            output.print("<b>... </b> &nbsp;&nbsp;");
                            
                            output.flush();
                            
                            equal = 0;

                            matchCounter++;
                        }
                    }
                    else
                    {
                        equal = 0;
                    }
                }
            }

            if (matchCounter > 0)
            {
                output.println("</span>");
                output.println("<br/>");
            }
            
            output.flush();
        }
        catch (IOException e)
        {
        	Logger.getLogger(getClass()).warn("fulltext search error", e);
        }
        finally
        {
        	if (fin != null) 
        	{
        		try
        		{
        			fin.close();
        		}
        		catch (Exception ex)
        		{
        		}
        	}
        }
    }
    
    private boolean searchInMetaInf(String fullPath, String searchArg) {
		ArrayList<String> tags = metaInfMgr.getTags(fullPath);
		if (tags != null) {
			for (String tag : tags) {
				if (tag.toLowerCase().indexOf(searchArg.toLowerCase()) >= 0) {
					return true;
				}
			}
		}
		
		String description = metaInfMgr.getDescription(fullPath);
		if ((description != null) &&
			(description.toLowerCase().indexOf(searchArg.toLowerCase()) >= 0)) {
			return true;
		}
		return false;
    }
    
}
