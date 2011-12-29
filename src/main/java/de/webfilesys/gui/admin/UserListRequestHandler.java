package de.webfilesys.gui.admin;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.LanguageManager;
import de.webfilesys.Paging;
import de.webfilesys.user.TransientUser;
import de.webfilesys.user.UserComparator;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.UTF8URLEncoder;

/**
 * Show a pageable list of users.
 * 
 * @author Frank Hoehnel
 */
public class UserListRequestHandler extends AdminRequestHandler
{
    public static final String SESSION_KEY_USER_LIST_START_IDX = "userListStartIdx";
    public static final String SESSION_KEY_USER_LIST_PAGE_SIZE = "userListPageSize";
    public static final String SESSION_KEY_USER_LIST_SORT_FIELD = "userListSortField";
    public static final String SESSION_KEY_USER_LIST_FILTER = "userListFilter";
    
	public UserListRequestHandler(
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
		output.print("<HTML>");
		output.print("<HEAD>");

		output.println("<SCRIPT LANGUAGE=\"JavaScript\">");
		output.println("function confirmDelete(delUser)");
		output.println("{if (confirm(\"Are you sure you want to delete user \" + delUser + \" ?\"))");
		output.println("    {window.location=\"/webfilesys/servlet?command=admin&cmd=deleteUser&userToBeDeleted=\" + delUser ;");
		output.println("    }");
		output.println("}");

		output.println("function diskQuota(userid) {window.open('/webfilesys/servlet?command=diskQuota&userid=' + encodeURIComponent(userid) + '&random=' + new Date().getTime(),'quotaWin','scrollbars=no,resizable=no,width=400,height=230,left=100,top=100,screenX=100,screenY=100');}");

		output.println("</SCRIPT>");

		output.print("<TITLE> WebFileSys User Administration </TITLE>");

		output.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"/webfilesys/css/" + userMgr.getCSS(uid) + ".css\">");

		output.println("</HEAD>");
		output.println("<BODY>");

		headLine("WebFileSys User Administration");

		output.println("<br/>");

		HttpSession session = req.getSession(true);
		
		String initial = req.getParameter("initial");
		
		int pageSize = Paging.DEFAULT_PAGE_SIZE;

		if (initial == null) 
		{
	        String pageSizeParm = getParameter(Paging.PARAM_PAGE_SIZE);

	        if (pageSizeParm != null)
	        {
	            try
	            {
	                pageSize = Integer.parseInt(pageSizeParm);
	                session.setAttribute(SESSION_KEY_USER_LIST_PAGE_SIZE, new Integer(pageSize));
	            }
	            catch (NumberFormatException nfex)
	            {
	            }
	        } else {
	            Integer userListPageSize = (Integer) session.getAttribute(SESSION_KEY_USER_LIST_PAGE_SIZE);

	            if (userListPageSize != null) {
	                pageSize = userListPageSize.intValue();
	            }
	        }
		} 
		else 
		{
            session.removeAttribute(SESSION_KEY_USER_LIST_PAGE_SIZE);
		}

		int startIdx=0;

        if (initial == null) 
        {
            String startIdxParm=getParameter(Paging.PARAM_START_INDEX);

            if (startIdxParm!=null)
            {
                try
                {
                    startIdx = Integer.parseInt(startIdxParm);
                    session.setAttribute(SESSION_KEY_USER_LIST_START_IDX, new Integer(startIdx));
                }
                catch (NumberFormatException nfex)
                {
                }
            } 
            else 
            {
                Integer userListStartIdx = (Integer) session.getAttribute(SESSION_KEY_USER_LIST_START_IDX);
                if (userListStartIdx != null) 
                {
                    startIdx = userListStartIdx.intValue();
                }
            }
        }
        else 
        {
            session.removeAttribute(SESSION_KEY_USER_LIST_START_IDX);
        }

        String searchMask = "";
 
        if (initial == null) {
            String filter = getParameter("searchMask");

            if (filter != null)
            {
                searchMask = filter;
                session.setAttribute(SESSION_KEY_USER_LIST_FILTER, filter);
            }
            else
            {
                String userListFilter = (String) session.getAttribute(SESSION_KEY_USER_LIST_FILTER);
                if (userListFilter != null) {
                    searchMask = userListFilter;
                }
            }
        }
        else 
        {
            session.removeAttribute(SESSION_KEY_USER_LIST_FILTER);
        }
        
		int sortBy = UserComparator.SORT_BY_USERID;

		if (initial == null) 
		{
	        String sortParm = getParameter("sortField");
	        
	        if (sortParm != null)
	        {
	            try
	            {
	                sortBy = Integer.parseInt(sortParm);
                    session.setAttribute(SESSION_KEY_USER_LIST_SORT_FIELD, new Integer(sortBy));
	            }
	            catch (NumberFormatException nfex)
	            {
	            }
	        }
            else 
            {
                Integer userListSortField = (Integer) session.getAttribute(SESSION_KEY_USER_LIST_SORT_FIELD);
                if (userListSortField != null) 
                {
                    sortBy = userListSortField.intValue();
                }
            }
		}
        else 
        {
            session.removeAttribute(SESSION_KEY_USER_LIST_SORT_FIELD);
        }

		output.println("<form accept-charset=\"utf-8\" method=\"get\" action=\"/webfilesys/servlet\">");

		output.println("<input type=\"hidden\" name=\"command\" value=\"admin\">");
		output.println("<input type=\"hidden\" name=\"cmd\" value=\"userList\">");
		
		output.println("<table width=\"100%\" border=\"0\">");
		output.println("<tr>");
		output.println("<td width=\"70%\">&nbsp;</td>");

		output.println("<td class=\"plaintext\" align=\"right\" nowrap>");
		output.println("sort by");
		output.println("<select name=\"sortField\" size=\"1\" onchange=\"document.forms[0].submit()\">");
		output.print("<option value=\"" + UserComparator.SORT_BY_USERID + "\"");
		if (sortBy==UserComparator.SORT_BY_USERID)
		{
			output.print(" selected");
		}
		output.println(">userid</option>");
		
		output.print("<option value=\"" + UserComparator.SORT_BY_LAST_NAME + "\"");
		if (sortBy==UserComparator.SORT_BY_LAST_NAME)
		{
			output.print(" selected");
		}
		output.println(">last name</option>");
        
		output.print("<option value=\"" + UserComparator.SORT_BY_FIRST_NAME + "\"");
		if (sortBy==UserComparator.SORT_BY_FIRST_NAME)
		{
			output.print(" selected");
		}
		output.println(">first name</option>");

		output.println("<option value=\"" + UserComparator.SORT_BY_ROLE + "\"");
		if (sortBy==UserComparator.SORT_BY_ROLE)
		{
			output.print(" selected");
		}
		output.println(">role</option>");
		
		output.print("<option value=\"" + UserComparator.SORT_BY_LAST_LOGIN + "\"");
		if (sortBy==UserComparator.SORT_BY_LAST_LOGIN)
		{
			output.print(" selected");
		}
		output.println(">last login</option>");
		output.println("</select>");
		output.println("</td>");

		output.println("<td>&nbsp;&nbsp;</td>");

		output.println("<td class=\"plaintext\" align=\"right\" nowrap>");
		output.println("filter: ");
		output.println("<input type=\"text\" name=\"searchMask\" size=\"10\" maxlength=\"32\" value=\"" + searchMask + "\" style=\"width:80px\">");
		output.println("</td>");

		output.println("<td>&nbsp;&nbsp;</td>");

		output.println("<td class=\"plaintext\" align=\"right\" nowrap>");
		output.println("elements per page: ");
		output.println("<input type=\"text\" name=\"" + Paging.PARAM_PAGE_SIZE + "\" maxlength=\"3\" maxlength=\"3\" value=\"" + pageSize + "\" style=\"width:40px\">");
		output.println("<input type=\"submit\" value=\"Refresh\">");               
		output.println("</td></tr></table>");
		output.println("</form>");

		output.println("<table width=\"100%\" border=\"1\" cellspacing=\"0\">");
		output.println("<tr bgcolor=lavender>");
		output.println("<th class=\"datahead\">&nbsp;</th><th class=\"datahead\">userid/login</th><th class=\"datahead\">document root</th><th class=\"datahead\">readonly</th><th class=\"datahead\">role</th><th class=\"datahead\">language</th><th class=\"datahead\">last name, first name</th><th class=\"datahead\">e-mail</th><th class=\"datahead\">last login</th></tr>");

		SimpleDateFormat dateFormat=LanguageManager.getInstance().getDateFormat("admin");

		Vector allUsers = userMgr.getRealUsers();
        
		if (allUsers.size()>1)
		{
			Collections.sort(allUsers,new UserComparator(sortBy));
		}

		if ((searchMask!=null) && (searchMask.trim().length()>0))
		{
			for (int i=allUsers.size()-1;i>=0;i--)
			{
				TransientUser user = (TransientUser) allUsers.elementAt(i);
        		
				if ((!CommonUtils.containsString(user.getUserid(),searchMask)) &&
					(!CommonUtils.containsString(user.getLastName(),searchMask)) &&
					(!CommonUtils.containsString(user.getFirstName(),searchMask)) &&
					(!CommonUtils.containsString(user.getEmail(),searchMask)))
				{
					allUsers.removeElementAt(i);
				}
			}
        	
			// allUserNames=filterUsers(allUserNames,searchMask);
		}
 
		Paging paging=new Paging(allUsers,pageSize,startIdx);

		Vector usersOnPage=paging.getElementVector();

		for (int i=0;i<usersOnPage.size();i++)
		{
			TransientUser actUser=(TransientUser) usersOnPage.elementAt(i);

			output.println("<tr>");

			output.print("<td class=\"data\" align=\"left\" valign=\"top\" nowrap>");
			output.print("<a href=\"javascript:confirmDelete('" + actUser.getUserid() + "')\"><img src=\"images/trash.gif\" alt=\"Delete User\" border=\"0\"></a>");
			output.print("<a href=\"/webfilesys/servlet?command=admin&cmd=editUser&username=" + UTF8URLEncoder.encode(actUser.getUserid()) + "\"><img src=\"images/edit2.gif\" alt=\"Edit User\" border=0></a>");

			if (actUser.getDiskQuota() > 0)
			{
				output.print("<a href=\"javascript:diskQuota('" + actUser.getUserid() + "')\"><img src=\"images/barGraph.gif\" alt=\"Disk Quota Usage\" border=0></a>");
			}

			output.println("</td>");

			output.println("<td class=\"data\" align=\"left\" valign=\"top\">" + actUser.getUserid() + " </td>");

			String docRoot = actUser.getDocumentRoot();
			
			String shortDocRoot = docRoot;

			if ((docRoot == null) || (docRoot.trim().length() == 0))
			{
				shortDocRoot = "&nbsp;";
			}
			else
			{
				if (docRoot.length() > 40)
				{
					shortDocRoot = docRoot.substring(0,10) + "..." + docRoot.substring(docRoot.length() - 26);
				}
			}

			output.println("<td class=\"data\" valign=\"top\" nowrap=\"true\">");
			
            String title = "";
            
			if (docRoot.length() > 40)
			{
                title = docRoot;
			}

            output.println("<span title=\"" + title + "\">");
            
			output.print(shortDocRoot);
			
			if ((File.separatorChar=='\\') && (docRoot.equals("*:") || docRoot.equals("*:/")))
			{
				output.println(" (all drives)");
			}

		    output.println("</span>");

			output.println("</td>");

			output.println("<td class=\"data\" valign=\"top\">" + actUser.isReadonly() + "</td>");

			String role=actUser.getRole();
            
			if ((role==null) || (role.trim().length()==0))
			{
				role="&nbsp;";
			}

			output.println("<td class=\"data\" valign=\"top\"> " + role + "</td>");

			String userLanguage=actUser.getLanguage();

			if (userLanguage==null)
			{
				userLanguage=LanguageManager.DEFAULT_LANGUAGE;
			}

			output.println("<td class=\"data\" valign=\"top\"> " + userLanguage + "</td>");

			String lastName=actUser.getLastName();
			String firstName=actUser.getFirstName();

			StringBuffer fullName=new StringBuffer();
			if ((lastName!=null) && (lastName.trim().length()>0))
			{
				fullName.append(lastName);
				if ((firstName!=null) && (firstName.trim().length()>0)) 
				{
				    fullName.append(", ");
				}
			}

			if ((firstName!=null) && (firstName.trim().length()>0))
			{
				fullName.append(firstName);
			}

			if (fullName.length()==0)
			{
				fullName.append("&nbsp;");
			}

			output.println("<td class=\"data\" valign=\"top\"> " + fullName.toString() + "</td>");

			String email=actUser.getEmail();

			if ((email==null) || (email.trim().length()==0))
			{
				output.println("<td class=\"data\">&nbsp;</td>");
			}
			else
			{
				int atSignIdx=email.indexOf('@');
				String formattedEmail=email.substring(0,atSignIdx+1) + " " + email.substring(atSignIdx+1);
				output.println("<td class=\"data\"><a class=\"fn\" href=\"mailto:" + email + "\">" + formattedEmail + "</a></td>");
			}

			Date lastLogin=actUser.getLastLogin();

			output.println("<td class=\"data\">");

			if (lastLogin==null)
			{
				output.println("&nbsp;");
			}
			else
			{
				output.println(dateFormat.format(lastLogin));
			}
			output.println("</td>");

			output.println("</tr>");
		}

		output.println("</table><br>");


		if (paging.getElementNumber()>0)
		{
			output.println("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");

			output.println("<tr>");
			output.println("<td class=\"dir\" align=\"left\" valign=\"middle\" nowrap=\"true\">");
			output.println("&nbsp;");

			if (paging.isFirstPage())
			{
				output.println("<img src=\"images/firstDisabled.gif\" border=\"0\">");
				output.println("<img src=\"images/previousDisabled.gif\" border=\"0\">");
			}
			else
			{
				output.print("<a href=\"/webfilesys/servlet?command=admin&cmd=userList&startIndex=0\">");
				output.println("<img src=\"images/first.gif\" border=\"0\"></a>");

				output.println("&nbsp;");

				output.print("<a href=\"/webfilesys/servlet?command=admin&cmd=userList&startIndex=" + paging.getPrevStartIndex() + "\">");
				output.println("<img src=\"images/previous.gif\" border=\"0\"></a>");
			}

			output.println("</td>");

			output.println("<td class=\"plaintext\" align=\"center\" valign=\"middle\">");
			output.println("elements ");
			output.println(paging.getStartIndex() + " - " + paging.getEndIndex() + " of " + paging.getElementNumber());

			output.println("&nbsp;&nbsp;&nbsp;page&nbsp;");

			// if there are more than 30 pages we show not all pages in the list
			int pageStep=paging.getElementNumber() / pageSize / 30 + 1;

			Enumeration startIndices=paging.getStartIndices();

			int pageCounter=1;

			while (startIndices.hasMoreElements())
			{
				int idx=((Integer) startIndices.nextElement()).intValue();

				if (idx!=(paging.getStartIndex()-1))
				{
					if (((pageCounter-1) % pageStep == 0) ||
						(!startIndices.hasMoreElements()))
					{
						output.print("<a class=\"fn\" href=\"/webfilesys/servlet?command=admin&cmd=userList&startIndex=" + idx + "\">");
						output.print(pageCounter);
						output.print("</a>");
					}
				}
				else
				{
					output.print(pageCounter);
				}

				output.println("&nbsp;");
				
				pageCounter++;
			}

			output.println("</td>");

			output.println("<td class=\"dir\" align=\"right\" valign=\"middle\" nowrap=\"true\">");

			if (!paging.isLastPage())
			{
				output.print("<a href=\"/webfilesys/servlet?command=admin&cmd=userList&startIndex=" + paging.getNextStartIndex() + "\">");
				output.println("<img src=\"images/next.gif\" border=\"0\"></a>");

				output.println("&nbsp;");
				
				output.print("<a href=\"/webfilesys/servlet?command=admin&cmd=userList&startIndex=" + paging.getLastPageStartIndex() + "\">");
				output.println("<img src=\"images/last.gif\" border=\"0\"></a>");
			}
			else
			{
				output.println("<img src=\"images/nextDisabled.gif\" border=\"0\">");
				output.println("<img src=\"images/lastDisabled.gif\" border=\"0\">");
			}

			output.println("&nbsp;");
			output.println("</td>");
			output.println("</tr>");
			output.println("</table>");
		}
		else
		{
			output.println("no entries");
		}

		output.println("<form accept-charset=\"utf-8\" style=\"margin-top:20px;\">");

		output.println("<input type=\"button\" value=\"Add new user\" onclick=\"window.location.href='/webfilesys/servlet?command=admin&cmd=registerUser'\">");

		output.println("&nbsp;&nbsp;&nbsp;");

		output.println("<input type=\"button\" value=\"Return\" onclick=\"window.location.href='/webfilesys/servlet?command=admin&cmd=menu'\">");

		output.println("</form>");

		output.println("</body></html>");
		output.flush();
	}

}
