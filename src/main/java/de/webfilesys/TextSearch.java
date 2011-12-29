package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

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
    int hit_num;

    HttpSession session;
    
    boolean readonly = false;
    
    String searchResultDir = null;
    
    private String uid = null;

    boolean includeSubdirs;
    boolean includeMetaInf;
    boolean metaInfOnly;
    
    Category category = null;

    MetaInfManager metaInfMgr=null;

    Vector searchArgs;
    
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
        hit_num=0;

        metaInfMgr=MetaInfManager.getInstance();

        this.includeSubdirs = includeSubdirs;
        this.includeMetaInf=includeMetaInf;
        this.metaInfOnly=metaInfOnly;
        
        this.category = category;

        getSearchArgs(searchString);

        search_tree(root_dir,file_mask,fromDate.getTime(),toDate.getTime(),
                    relativePath);

        output.println("<script language=\"javascript\">");
        output.println("document.getElementById('currentSearchDir').style.visibility='hidden';");
        output.println("</script>");
    }

    public int getHitNumber()
    {
        return(hit_num);
    }

    public void getSearchArgs(String searchedText)
    {
        searchArgs=new Vector();

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
                    searchArgs.addElement(searchWord.toString());
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
                            searchArgs.addElement(searchWord.toString());
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
            searchArgs.addElement(searchWord.toString());
        }
    }

    public void search_tree(String act_path,String file_mask, long fromDate, long toDate,
                            String relativePath)
    {
        if (session.getAttribute("searchCanceled")!=null)
        {
            return;
        }

        File dir_file;
        File temp_file;
        int i;
        String sub_dir;
        String file_list[]=null;
        boolean all_words_found;

        int pathLength = relativePath.length();
        String shortPath = relativePath;

        if (pathLength>60)
        {
            shortPath = relativePath.substring(0,17) + "..." + relativePath.substring(pathLength-40);
        }

        if (session.getAttribute("searchCanceled")==null)
        {
            output.println("<script language=\"javascript\">");
			output.println("document.getElementById('currentSearchDir').innerHTML='" + insertDoubleBackslash(shortPath) + "';");
            output.println("</script>");
            output.flush();
        }

        dir_file=new File(act_path);
        file_list=dir_file.list();

        if (file_list!=null)
        {
            for (i=0;(session.getAttribute("searchCanceled")==null) && (i<file_list.length);i++)
            {
                String fullPath = null;
                
                String relativeFile = null;

                if (act_path.endsWith(File.separator))
                {
                    fullPath = act_path + file_list[i];
                    
                    relativeFile = relativePath + file_list[i];
                }
                else
                {
                    fullPath=act_path + File.separator + file_list[i];

					relativeFile = relativePath + File.separator + file_list[i];
                }
                
                temp_file=new File(fullPath);

                if (temp_file.isDirectory())
                {
                	if (includeSubdirs)
                	{
                        if (!dirIsLink(temp_file))
                        {
    						if (!file_list[i].equals(ThumbnailThread.THUMBNAIL_SUBDIR))
    						{
    							String relativeSubPath = null;
    							
    							if (act_path.endsWith(File.separator))
    							{
    								sub_dir = act_path + file_list[i];
    								
    								relativeSubPath = relativePath + file_list[i];
    							}
    							else
    							{
    								sub_dir = act_path + File.separator + file_list[i];

    								relativeSubPath = relativePath + File.separator + file_list[i];
    							}
    							
    							search_tree(sub_dir, file_mask, fromDate, toDate, relativeSubPath);
    						}
                        }
                	}
                }
                else
                {
                    if ((temp_file.lastModified()>=fromDate) &&
                        (temp_file.lastModified()<=toDate))
                    {
                        if (PatternComparator.patternMatch(file_list[i],file_mask))
                        {
                        	if ((category == null) || metaInfMgr.isCategoryAssigned(fullPath, category))
                        	{
								all_words_found=true;

								int firstMatchIdx[]=new int[searchArgs.size()];

								for (int j=0;(j<searchArgs.size()) && all_words_found;j++)
								{
									firstMatchIdx[j]=(-1);

									if (metaInfOnly)
									{
										String description=metaInfMgr.getDescription(fullPath);

										if ((description==null) ||
											(description.toLowerCase().indexOf(((String) searchArgs.elementAt(j)).toLowerCase()) < 0))
										{
											all_words_found=false;
										}
									}
									else
									{
										// if (includeMetaInf && metaInfMgr.isMetaInfFile(fullPath))
										if (metaInfMgr.isMetaInfFile(fullPath))
										{
											all_words_found=false;
										}
										else
										{
											firstMatchIdx[j]=locateTextInFile(temp_file.toString(),(String) searchArgs.elementAt(j));
                                        
											if (firstMatchIdx[j] < 0)
											{
												if (!includeMetaInf)
												{
													all_words_found=false;
												}
												else
												{
													String description=metaInfMgr.getDescription(fullPath);

													if ((description==null) ||
														(description.toLowerCase().indexOf(((String) searchArgs.elementAt(j)).toLowerCase()) < 0))
													{
														all_words_found=false;
													}
												}
											}
										}
									}
								}
                            
								if (all_words_found)
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
                                            iconImg = IconManager.getInstance().getIconForFileName(file_list[i]);
                                        }
									    
										output.println("<a class=\"fn\" href=\"" + viewLink + "\" target=\"_blank\"><img border=\"0\" src=\"icons/" + iconImg + "\" align=\"absbottom\" style=\"margin-top:8px;\"> " + relativeFile + "</a>");
										output.println("<br/>");
										output.flush();
										hit_num++;

										printMatches(fullPath,firstMatchIdx);
										
										if (!readonly)
										{
											try
											{
												metaInfMgr.createLink(searchResultDir, new FileLink(file_list[i], fullPath, uid));
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
        
        file_list=null;
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
                            file_input.close();

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

            file_input.close();
        }
        catch (IOException e)
        {
        	Logger.getLogger(getClass()).warn("fulltext search error: " + e);
        }

        return(-1);
    }

    protected void printMatches(String fileName,int firstMatchIdx[])
    {
        for (int i=0;i < searchArgs.size();i++)
        {
            if (firstMatchIdx[i] > 0)
            {
                printHitEnvironment(fileName,(String) searchArgs.elementAt(i),firstMatchIdx[i]);
            }
        }
    }

    protected void printHitEnvironment(String fileName,String searched,int firstMatchIdx)
    {
        int equal;

        int searchLength=searched.length();

        FileInputStream fin = null;

        try
        {
            fin = new FileInputStream(fileName);
        }
        catch (FileNotFoundException e)
        {
            Logger.getLogger(getClass()).error("cannot read file containing search hit", e);
            return;
        }

        char resultBuff[] = new char[searchLength + 10]; 

        for (int i = 0; i < resultBuff.length; i++)
        {
            resultBuff[i] = ' ';
        }
        
        int hitNum=0;

        int count=0;

        equal=0;

        try
        {
            int startIdx=firstMatchIdx - 10;

            if (startIdx < 0)
            {
                startIdx=0;
            }

            for (int i=0;i<startIdx;i++)
            {
                if (fin.read() < 0)
                {
                    Logger.getLogger(getClass()).warn("cannot locate to search hit index " + firstMatchIdx);
                }
            }

            while ((hitNum < 5) && ((count = fin.read(buffer))>=0))
            {
                for (int i=0;(hitNum < 5) && (i<count);i++)
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

                    if (Character.toUpperCase(ch)==Character.toUpperCase(searched.charAt(equal)))
                    {
                        if (++equal==searchLength)
                        {
                            if (hitNum == 0)
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

                            hitNum++;
                        }
                    }
                    else
                    {
                        equal = 0;
                    }
                }
            }

            if (hitNum > 0)
            {
                output.println("</span>");
                output.println("<br/>");
            }
            
            output.flush();

            fin.close();
        }
        catch (IOException e)
        {
        	Logger.getLogger(getClass()).warn("fulltext search error: " + e);
        }
    }
    
	protected boolean dirIsLink(File f)
	{
		if (File.separatorChar!='/')
		{
			return(false);
		}

		try
		{
			return(!(f.getCanonicalPath().equals(f.getAbsolutePath())));
		}
		catch (IOException ioex)
		{
			Logger.getLogger(getClass()).error(ioex);
			return(false);
		}
	}
    
	private String insertDoubleBackslash(String source)
	{
		StringBuffer dest=new StringBuffer();

		for (int i=0;i<source.length();i++)
		{
			if (source.charAt(i)=='\\')
			{
				dest.append("\\\\");
			}
			else
			{
				dest.append(source.charAt(i));
			}
		}    
		return(dest.toString());
	}
}
