package de.webfilesys;

import java.io.BufferedInputStream;
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
    String root_dir;
    String search_arg;
    String full_path;
    byte buffer[];
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

    ArrayList<String> searchArgs;
    
    public TextSearch(String root_dir,String file_mask,String searchString,Date fromDate,Date toDate,
               PrintWriter output, boolean includeSubdirs, boolean includeMetaInf,boolean metaInfOnly,
               Category category, String searchResultDir,
               HttpSession session, boolean readonly,
               String relativePath,
               String uid)
    {
        this.root_dir=root_dir;
        this.search_arg=searchString;
        this.output=output;
        this.searchResultDir = searchResultDir;
        this.session=session;
        this.readonly = readonly;
        this.uid = uid;

        buffer = new byte[4096];
        hitNum = 0;

        metaInfMgr = MetaInfManager.getInstance();

        this.includeSubdirs = includeSubdirs;
        this.includeMetaInf=includeMetaInf;
        this.metaInfOnly=metaInfOnly;
        
        this.category = category;

        getSearchArgs(searchString);

        search_tree(root_dir,file_mask,fromDate.getTime(),toDate.getTime(), relativePath);

        if (hitNum > 0) 
        {
        	Decoration deco = new Decoration();
        	deco.setIcon("search.gif");
        	deco.setTextColor("#808080");
        	DecorationManager.getInstance().setDecoration(searchResultDir, deco);
        }
        
        output.flush();
    }

    public int getHitNumber()
    {
        return(hitNum);
    }

    public void getSearchArgs(String searchedText)
    {
        searchArgs = new ArrayList<String>();

        boolean quoted=false;
        boolean wordStopped=true;

        StringBuffer searchWord=new StringBuffer();

        for (int i=0;i<searchedText.length();i++)
        {
            char actChar=searchedText.charAt(i);

            if (actChar=='\"')
            {
                if (quoted)
                {
                    searchArgs.add(searchWord.toString());
                    quoted=false;
                    wordStopped=true;
                }
                else
                {
                    quoted=true;
                }
            }
            else
            {
                if (actChar==' ')
                {
                    if (quoted)
                    {
                        searchWord.append(actChar);
                    }
                    else
                    {
                        if (!wordStopped)
                        {
                            searchArgs.add(searchWord.toString());
                            wordStopped=true;
                        }
                    }
                }
                else
                {
                    if (wordStopped)
                    {
                        searchWord=new StringBuffer();
                        wordStopped=false;
                    }
                    searchWord.append(actChar);
                }
            }
        }

        if (!wordStopped)
        {
            searchArgs.add(searchWord.toString());
        }
    }

    public void search_tree(String act_path,String file_mask, long fromDate, long toDate,
                            String relativePath)
    {
        if (session.getAttribute("searchCanceled")!=null)
        {
            return;
        }

        String shortPath = CommonUtils.shortName(relativePath, 70);
        
        if (session.getAttribute("searchCanceled") == null)
        {
			output.println("<div class=\"searchPath\"><span class=\"searchPath\">" + shortPath + "</span></div>");
            output.flush();
        }

        File dirFile = new File(act_path);
        String[] fileList = dirFile.list();

        if (fileList != null)
        {
            for (int i = 0;(session.getAttribute("searchCanceled") == null) && (i < fileList.length);i++)
            {
                String fullPath = null;
                
                String relativeFile = null;

                if (act_path.endsWith(File.separator))
                {
                    fullPath = act_path + fileList[i];
                    
                    relativeFile = relativePath + fileList[i];
                }
                else
                {
                    fullPath=act_path + File.separator + fileList[i];

					relativeFile = relativePath + File.separator + fileList[i];
                }
                
                File tempFile = new File(fullPath);

                if (tempFile.isDirectory())
                {
                	if (includeSubdirs)
                	{
                        if (!CommonUtils.dirIsLink(tempFile))
                        {
    						if (!fileList[i].equals(ThumbnailThread.THUMBNAIL_SUBDIR))
    						{
    							String relativeSubPath = null;
    							
    					        String subDir;
    							
    							if (act_path.endsWith(File.separator))
    							{
    								subDir = act_path + fileList[i];
    								
    								relativeSubPath = relativePath + fileList[i];
    							}
    							else
    							{
    								subDir = act_path + File.separator + fileList[i];

    								relativeSubPath = relativePath + File.separator + fileList[i];
    							}
    							
    							search_tree(subDir, file_mask, fromDate, toDate, relativeSubPath);
    						}
                        }
                	}
                }
                else
                {
                    if ((tempFile.lastModified()>=fromDate) &&
                        (tempFile.lastModified()<=toDate))
                    {
                        if (PatternComparator.patternMatch(fileList[i],file_mask))
                        {
                        	if ((category == null) || metaInfMgr.isCategoryAssigned(fullPath, category))
                        	{
								boolean allWordsFound = true;

								int firstMatchIdx[]=new int[searchArgs.size()];

								for (int j=0;(j<searchArgs.size()) && allWordsFound;j++)
								{
									firstMatchIdx[j]=(-1);

									if (metaInfOnly)
									{
										String description = metaInfMgr.getDescription(fullPath);

										if ((description == null) ||
											(description.toLowerCase().indexOf(searchArgs.get(j).toLowerCase()) < 0))
										{
											allWordsFound=false;
										}
									}
									else
									{
										// if (includeMetaInf && metaInfMgr.isMetaInfFile(fullPath))
										if (metaInfMgr.isMetaInfFile(fullPath))
										{
											allWordsFound=false;
										}
										else
										{
											firstMatchIdx[j]=locateTextInFile(tempFile.toString(), searchArgs.get(j));
                                        
											if (firstMatchIdx[j] < 0)
											{
												if (!includeMetaInf)
												{
													allWordsFound=false;
												}
												else
												{
													String description=metaInfMgr.getDescription(fullPath);

													if ((description==null) ||
														(description.toLowerCase().indexOf(searchArgs.get(j).toLowerCase()) < 0))
													{
														allWordsFound=false;
													}
												}
											}
										}
									}
								}
                            
								if (allWordsFound)
								{
                                    String viewLink = null;
                                    try
                                    {
                                        viewLink = "/webfilesys/servlet?command=getFile&filePath=" + URLEncoder.encode(fullPath, "UTF-8");
                                    }
                                    catch (UnsupportedEncodingException uex)
                                    {
                                        // should never happen
                                    }

									if (session.getAttribute("searchCanceled")==null)
									{
                                        String iconImg = "doc.gif";

                                        if (WebFileSys.getInstance().isShowAssignedIcons())
                                        {
                                            iconImg = IconManager.getInstance().getIconForFileName(fileList[i]);
                                        }
									    
										output.println("<a class=\"fn\" href=\"" + viewLink + "\" target=\"_blank\"><img border=\"0\" src=\"icons/" + iconImg + "\" align=\"absbottom\" style=\"margin-top:8px;\"> " + relativeFile + "</a>");
										output.println("<br/>");
										output.flush();
										hitNum++;

										printMatches(fullPath,firstMatchIdx);
										
										if (!readonly)
										{
											try
											{
												metaInfMgr.createLink(searchResultDir, new FileLink(fileList[i], fullPath, uid), true);
											}
											catch (FileNotFoundException nfex)
											{
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

            if ((category != null) || includeMetaInf || metaInfOnly)
            {
                if (!act_path.equals(searchResultDir))
                {
                    metaInfMgr.releaseMetaInf(act_path);
                }
            }
        }
        else
        {
            output.println("cannot get dir entries for " + act_path);
            output.println("<br/>");
            output.flush();
        }
        
        fileList=null;
    }

    public int locateTextInFile(String act_file,String search_arg)
    {
        int i;
        int equal;
        int search_length;

        search_length=search_arg.length();

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

        equal=0;

        try
        {
            while (( count = file_input.read(buffer))>=0 )
            {
                for (i=0;i<count;i++)
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
        for (int i = 0; i < searchArgs.size(); i++)
        {
            if (firstMatchIdx[i] > 0)
            {
                printHitEnvironment(fileName, searchArgs.get(i), firstMatchIdx[i]);
            }
        }
    }

    protected void printHitEnvironment(String fileName,String searched,int firstMatchIdx)
    {
        int searchLength = searched.length();

        FileInputStream fin = null;

        try
        {
            fin = new FileInputStream(fileName);
        }
        catch (FileNotFoundException e)
        {
            Logger.getLogger(getClass()).error("cannot read file containing search match", e);
            return;
        }

        char resultBuff[] = new char[searchLength + 10]; 

        for (int i = 0; i < resultBuff.length; i++)
        {
            resultBuff[i] = ' ';
        }
        
        int matchCounter = 0;

        int count=0;

        int equal=0;

        try
        {
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

                            output.print("<b>");

                            String matchText = new String(resultBuff, resultBuff.length-searchLength, searchLength);
                            output.print(CommonUtils.escapeHTML(matchText));

                            output.print("</b>");

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
    
}
