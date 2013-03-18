package de.webfilesys.gui.xsl.calendar;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslCalendarHandler extends XslCalendarHandlerBase
{
	public XslCalendarHandler(
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
		GregorianCalendar nowCal = new GregorianCalendar();  
		
		String yearParam = req.getParameter("year");
		
		int year = 0;

		if ((yearParam == null) || (yearParam.length() == 0)) 
		{
			year = nowCal.get(Calendar.YEAR);
		}
		else 
		{
			try 
			{
				year = Integer.parseInt(yearParam);
			}
			catch (NumberFormatException numEx)
			{
				Logger.getLogger(getClass()).error("invalid parameter year: " + yearParam);
				year = nowCal.get(Calendar.YEAR);
			}
		}
		
		Element calendarElement = doc.createElement("calendar");
		
		calendarElement.setAttribute("year", Integer.toString(year));
			
		doc.appendChild(calendarElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/calendar/sunburstCalendar.xsl\"");

		doc.insertBefore(xslRef, calendarElement);

		XmlUtil.setChildText(calendarElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(calendarElement, "userid", uid, false);
		
		addCalendarResources();
		
		addMsgResource("calendar.titleYear", getResource("calendar.titleYear","Calendar Year"));
		
		addMsgResource("calendar.prevYear", getResource("calendar.prevYear","previous year"));
		addMsgResource("calendar.nextYear", getResource("calendar.nextYear","next year"));

		Calendar cal = new GregorianCalendar();
		
		cal.set(Calendar.YEAR, year);
		
		cal.setFirstDayOfWeek(Calendar.MONDAY);

		boolean currentYear = (nowCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR));
		
		Element monthListElem = doc.createElement("monthList");
		calendarElement.appendChild(monthListElem);
		
		for (int month = 0; month < 12; month++)
		{
            Element monthElem = doc.createElement("month");
            monthElem.setAttribute("id", Integer.toString(month));
			
			cal.set(Calendar.MONTH, month);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			
			monthElem.setAttribute("startDay", Integer.toString(cal.get(Calendar.DAY_OF_YEAR) - 1));
			
			monthElem.setAttribute("endDay", Integer.toString(getLastDayOfMonth(cal) - 1));
			
			if (currentYear)
			{
				if (nowCal.get(Calendar.MONTH) == month)
				{
					monthElem.setAttribute("current", "true");
				}
			}
			
			monthListElem.appendChild(monthElem);
		}

		Element weekListElem = doc.createElement("weekList");
		calendarElement.appendChild(weekListElem);

		cal.set(Calendar.YEAR, year);

		cal.set(Calendar.DAY_OF_YEAR, 1);
		
		boolean stop = false;
        boolean newWeek = true;
        
        int dayCounter = 0;

        Element weekElem = null;
        
		while (!stop)
		{
			dayCounter++;
			
			if (newWeek) 
			{
				weekElem = doc.createElement("week");
				weekListElem.appendChild(weekElem);
				weekElem.setAttribute("id", Integer.toString(cal.get(Calendar.WEEK_OF_YEAR) - 1));
				weekElem.setAttribute("startDay", Integer.toString(cal.get(Calendar.DAY_OF_YEAR) - 1));
				newWeek = false;
			}
			
			Element dayElem = doc.createElement("day");
			dayElem.setAttribute("id", Integer.toString(cal.get(Calendar.DAY_OF_YEAR) - 1));
			dayElem.setAttribute("dayOfMonth", Integer.toString(cal.get(Calendar.DAY_OF_MONTH) - 1));
			dayElem.setAttribute("dayOfWeek", Integer.toString((cal.get(Calendar.DAY_OF_WEEK) + 5) % 7));
			
			dayElem.setAttribute("month", Integer.toString(cal.get(Calendar.MONTH)));
			weekElem.appendChild(dayElem);
			
			if (currentYear) 
			{
				if (nowCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR))
				{
					dayElem.setAttribute("today", "true");
					weekElem.setAttribute("current", "true");
				}
			}
			
			cal.add(Calendar.DAY_OF_YEAR, 1);
			
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
			{
				weekElem.setAttribute("endDay", Integer.toString(cal.get(Calendar.DAY_OF_YEAR) - 2));
				newWeek = true;
			}
			
			if (cal.get(Calendar.DAY_OF_YEAR) == 1)
			{
				weekElem.setAttribute("endDay", Integer.toString(dayCounter - 1));
				stop = true;
			}
		}
		
		calendarElement.setAttribute("days", Integer.toString(dayCounter));
		
		if (currentYear) 
		{
		    calendarElement.setAttribute("current", "true");	
		}
		XmlUtil.setChildText(calendarElement, "prevYear", Integer.toString(year - 1));
		XmlUtil.setChildText(calendarElement, "nextYear", Integer.toString(year + 1));
		
		processResponse("calendar/sunburstCalendar.xsl", false);
    }
	
	private int getLastDayOfMonth(Calendar cal) 
	{
		int currentDay = cal.get(Calendar.DAY_OF_MONTH);
		int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		
		while (true) 
		{
			cal.add(Calendar.DAY_OF_MONTH, 1);
			int newDay = cal.get(Calendar.DAY_OF_MONTH);
			if (newDay < currentDay)
			{
				return dayOfYear;
			}
			currentDay = newDay;
			dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
		}
	}
}