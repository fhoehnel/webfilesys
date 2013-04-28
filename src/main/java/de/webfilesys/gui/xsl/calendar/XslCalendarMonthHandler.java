package de.webfilesys.gui.xsl.calendar;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

import de.webfilesys.calendar.Appointment;
import de.webfilesys.calendar.AppointmentManager;
import de.webfilesys.gui.ajax.calendar.XmlMoveAppointmentHandler;
import de.webfilesys.util.XmlUtil;

/**
 * @author Frank Hoehnel
 */
public class XslCalendarMonthHandler extends XslCalendarHandlerBase
{
	public XslCalendarMonthHandler(
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

		String monthParam = req.getParameter("month");
		
		int month = 0;

		if ((monthParam == null) || (monthParam.length() == 0)) 
		{
			month = nowCal.get(Calendar.MONTH);
		}
		else 
		{
			try 
			{
				month = Integer.parseInt(monthParam);
			}
			catch (NumberFormatException numEx)
			{
				Logger.getLogger(getClass()).error("invalid parameter month: " + monthParam);
				month = nowCal.get(Calendar.MONTH);
			}
		}
		
		Element calendarElement = doc.createElement("calendar");
		
		calendarElement.setAttribute("year", Integer.toString(year));
		calendarElement.setAttribute("month", Integer.toString(month));
			
		doc.appendChild(calendarElement);
			
		ProcessingInstruction xslRef = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"/webfilesys/xsl/calendar/calendarMonth.xsl\"");

		doc.insertBefore(xslRef, calendarElement);

		XmlUtil.setChildText(calendarElement, "css", userMgr.getCSS(uid), false);
		XmlUtil.setChildText(calendarElement, "userid", uid, false);
		
		addCalendarResources();
		
		addMsgResource("calendar.titleMonth", getResource("calendar.titleMonth","Calendar Month"));
		
		addMsgResource("calendar.mon", getResource("calendar.mon","Mon"));
		addMsgResource("calendar.tue", getResource("calendar.tue","Tue"));
		addMsgResource("calendar.wed", getResource("calendar.wed","Wed"));
		addMsgResource("calendar.thu", getResource("calendar.thu","Thu"));
		addMsgResource("calendar.fri", getResource("calendar.fri","Fri"));
		addMsgResource("calendar.sat", getResource("calendar.sat","Sat"));
		addMsgResource("calendar.sun", getResource("calendar.sun","Sun"));
		
		addMsgResource("appointment.startTime", getResource("appointment.startTime","start time"));
		addMsgResource("appointment.endTime", getResource("appointment.endTime","end time"));
		addMsgResource("appointment.subject", getResource("appointment.subject","subject"));
		addMsgResource("appointment.description", getResource("appointment.description","description"));
		
		addMsgResource("appointment.fullDay", getResource("appointment.fullDay","full day"));
		addMsgResource("appointment.multiDay", getResource("appointment.multiDay","several days"));
		addMsgResource("appointment.numOfDays", getResource("appointment.numOfDays","number of days"));
		
		addMsgResource("appointment.repeatPeriod", getResource("appointment.repeatPeriod","repeat period"));
		addMsgResource("appointment.repeatNone", getResource("appointment.repeatNone","none"));
		addMsgResource("appointment.repeatDaily", getResource("appointment.repeatDaily","daily"));
		addMsgResource("appointment.repeatWeekday", getResource("appointment.repeatWeekday","weekday"));
		addMsgResource("appointment.repeatWeekly", getResource("appointment.repeatWeekly","weekly"));
		addMsgResource("appointment.repeatMonthly", getResource("appointment.repeatMonthly","monthly"));
		addMsgResource("appointment.repeatYearly", getResource("appointment.repeatYearly","yearly"));
		
		addMsgResource("appointment.alarmType", getResource("appointment.alarmType","alarm type"));
		addMsgResource("appointment.alarmNone", getResource("appointment.alarmNone","none"));
		addMsgResource("appointment.alarmVisual", getResource("appointment.alarmVisual","visual"));
		addMsgResource("appointment.alarmSound", getResource("appointment.alarmSound","sound"));
		addMsgResource("appointment.alarmMail", getResource("appointment.alarmMail","e-mail"));
		addMsgResource("appointment.alarmAll", getResource("appointment.alarmAll","all types"));

		addMsgResource("appointment.alarmAhead", getResource("appointment.alarmAhead","alarm time before event"));

		addMsgResource("appointment.duration", getResource("appointment.duration","duration"));
		
		addMsgResource("appointment.buttonCreate", getResource("appointment.buttonCreate","create appointment"));
		addMsgResource("appointment.buttonSave", getResource("appointment.buttonSave","save changes"));
		addMsgResource("appointment.buttonDelete", getResource("appointment.buttonDelete","delete appointment"));
		addMsgResource("appointment.buttonCancel", getResource("appointment.buttonCancel","cancel"));
		addMsgResource("appointment.buttonMove", getResource("appointment.buttonMove","move"));
		addMsgResource("appointment.buttonPaste", getResource("appointment.buttonPaste","paste"));
		addMsgResource("appointment.hintPaste", getResource("appointment.hintPaste","Open the target day for the move operation and click the paste button!"));
		
		addMsgResource("appointment.confirmDelete", getResource("appointment.confirmDelete","Delete this appointment?"));
		
		Calendar cal = GregorianCalendar.getInstance(req.getLocale());
		
		cal.set(Calendar.YEAR, year);

		cal.set(Calendar.MONTH, month);
		
		cal.set(Calendar.DAY_OF_MONTH, 1);
		
		cal.setFirstDayOfWeek(Calendar.MONDAY);

		boolean currentYear = (nowCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR));

		boolean currentMonth = (nowCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH));

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        
        while (dayOfWeek != Calendar.MONDAY)
        {
        	if (cal.get(Calendar.DAY_OF_MONTH) == 1)
        	{
        		if (cal.get(Calendar.MONTH) == 0)
        		{
        		    cal.roll(Calendar.YEAR, false);	
            		cal.set(Calendar.MONTH, 11);
        		}
        		else
        		{
        			cal.roll(Calendar.MONTH, false);
        		}

        		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        	}
        	else
        	{
        		cal.roll(Calendar.DAY_OF_MONTH, false);
        	}
        	
        	dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        }

		Element weekListElem = doc.createElement("weekList");
		calendarElement.appendChild(weekListElem);
		
		boolean stop = false;
        boolean newWeek = true;
        
        boolean lastWeek = false;
        
        int dayCounter = 0;
        int prevMonthDays = 0;
        int nextMonthDays = 0;

        Element weekElem = null;
        
		while (!stop)
		{
			if (cal.get(Calendar.MONTH) == month)
			{
				dayCounter++;
			}
			else
			{
				if (lastWeek)
				{
					nextMonthDays++;
				}
				else
				{
					prevMonthDays++;
				}
			}
			
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
			
			long savedTime = cal.getTimeInMillis();
			
			addAppointments(cal, dayElem);
			
			cal.setTimeInMillis(savedTime);
			
			cal.add(Calendar.DAY_OF_YEAR, 1);
			
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
			{
				weekElem.setAttribute("endDay", Integer.toString(cal.get(Calendar.DAY_OF_YEAR) - 2));
				newWeek = true;

				if (lastWeek)
				{
					stop = true;
				}
			}
			
			if ((cal.get(Calendar.DAY_OF_MONTH) == 1) && (cal.get(Calendar.MONTH) != month))
			{
				lastWeek = true;
			}
		}
		
		calendarElement.setAttribute("days", Integer.toString(dayCounter));
		calendarElement.setAttribute("prevMonthDays", Integer.toString(prevMonthDays));
		calendarElement.setAttribute("nextMonthDays", Integer.toString(nextMonthDays));
		
		if (currentYear && currentMonth) 
		{
		    calendarElement.setAttribute("current", "true");	
		}
		
		int prevMonthYear = year;
		int prevMonth = month - 1;
		if (prevMonth < 0) 
		{
			prevMonth = 11;
			prevMonthYear--;
		}
		
		Element prevMonthElem = doc.createElement("prevMonth");
		calendarElement.appendChild(prevMonthElem);
		XmlUtil.setChildText(prevMonthElem, "year", Integer.toString(prevMonthYear));
		XmlUtil.setChildText(prevMonthElem, "month", Integer.toString(prevMonth));

		int nextMonthYear = year;
		int nextMonth = month + 1;
		if (nextMonth > 11) 
		{
			nextMonth = 0;
			nextMonthYear++;
		}
		
		Element nextMonthElem = doc.createElement("nextMonth");
		calendarElement.appendChild(nextMonthElem);
		XmlUtil.setChildText(nextMonthElem, "year", Integer.toString(nextMonthYear));
		XmlUtil.setChildText(nextMonthElem, "month", Integer.toString(nextMonth));
		
		if (readonly)
		{
			XmlUtil.setChildText(calendarElement, "readonly", Boolean.toString(true));
		}
		
		String appToMove = (String) req.getSession(true).getAttribute(XmlMoveAppointmentHandler.SESSION_KEY_APPOINTMENT_TO_MOVE);
		
		if (appToMove != null) 
		{
			XmlUtil.setChildText(calendarElement, "appointmentToMove", appToMove);
		}
		
		processResponse("calendar/calendarMonth.xsl", false);
    }
	
	private void addAppointments(Calendar cal, Element dayElem)
	{
		ArrayList<Appointment> appointments = AppointmentManager.getInstance().getAppointmentsForDay(cal, uid);
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long startOfDayTime = cal.getTimeInMillis();
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		long endOfDayTime = cal.getTimeInMillis();
		
		AppointmentManager.getInstance().addRepeatedAppointmentClones(uid, startOfDayTime, endOfDayTime, appointments);
		
        if (appointments.size() > 0)
        {
        	Calendar appCal = new GregorianCalendar();
        	
    		Element appointmentListElem = doc.createElement("appointmentList");
    		dayElem.appendChild(appointmentListElem);

    		Iterator<Appointment> iter = appointments.iterator();
        	while (iter.hasNext()) 
        	{
        		Appointment appointment = iter.next();
        		Element appointmentElem = doc.createElement("appointment");
        		appointmentListElem.appendChild(appointmentElem);
        		
        		XmlUtil.setChildText(appointmentElem, "id", appointment.getId());

        		XmlUtil.setChildText(appointmentElem, "eventTime", Long.toString(appointment.getEventTime().getTime()));
        		XmlUtil.setChildText(appointmentElem, "duration", Long.toString(appointment.getDuration()));
        		
        		appCal.setTime(appointment.getEventTime());
        		// String formattedStartTime = String.format("%2d:%2d", appCal.get(Calendar.HOUR_OF_DAY), appCal.get(Calendar.MINUTE));
        		String formattedStartTime = String.format("%1$tH:%1$tM", appCal);
        		XmlUtil.setChildText(appointmentElem, "startTime", formattedStartTime);
        		
        		int startTimeMinuteOfDay = appCal.get(Calendar.HOUR_OF_DAY) * 60 + appCal.get(Calendar.MINUTE);
        		XmlUtil.setChildText(appointmentElem, "startMinuteOfDay", Integer.toString(startTimeMinuteOfDay));
        		
        		long appointmentEndTime = appointment.getEventTime().getTime() + appointment.getDuration();
        		java.util.Date appointmentEndDate = new java.util.Date(appointmentEndTime);

        		appCal.setTime(appointmentEndDate);
        		
                /*
        		String formattedEndTime = String.format("%2d:%2d", appCal.get(Calendar.HOUR_OF_DAY), appCal.get(Calendar.MINUTE));
                */
        		String formattedEndTime = String.format("%1$tH:%1$tM", appCal.getTime());
        		XmlUtil.setChildText(appointmentElem, "endTime", formattedEndTime);

        		int endTimeMinuteOfDay = appCal.get(Calendar.HOUR_OF_DAY) * 60 + appCal.get(Calendar.MINUTE);
        		XmlUtil.setChildText(appointmentElem, "endMinuteOfDay", Integer.toString(endTimeMinuteOfDay));
        		
        		XmlUtil.setChildText(appointmentElem, "startHour", Integer.toString(appointment.getEventTime().getHours()));
        		
        		XmlUtil.setChildText(appointmentElem, "endHour", Integer.toString(appointmentEndDate.getHours()));

        		if ((appointment.getSubject() != null) && (appointment.getSubject().length() > 0))
        		{
            		XmlUtil.setChildText(appointmentElem, "subject", appointment.getSubject(), true);
        		}

        		XmlUtil.setChildText(appointmentElem, "repeatPeriod", Integer.toString(appointment.getRepeatPeriod()));
        		XmlUtil.setChildText(appointmentElem, "alarmType", Integer.toString(appointment.getAlarmType()));

        		int alarmAheadHours = 0;
        		int alarmAheadMinutes = 0;
        		Date alarmTime = appointment.getAlarmTime();
        		if (alarmTime != null)
        		{
        			long alarmAheadTime = appointment.getEventTime().getTime() - alarmTime.getTime();
        			alarmAheadHours = (int) (alarmAheadTime / (3600 * 1000));
        			alarmAheadMinutes = (int) ((alarmAheadTime % (3600 * 1000)) / (60 * 1000));
        		}
        		XmlUtil.setChildText(appointmentElem, "alarmAheadHours", Integer.toString(alarmAheadHours));
        		XmlUtil.setChildText(appointmentElem, "alarmAheadMinutes", Integer.toString(alarmAheadMinutes));
        		
        		if ((appointment.getContent() != null) && (appointment.getContent().length() > 0))
        		{
            		XmlUtil.setChildText(appointmentElem, "description", replaceLineBreak(appointment.getContent()), true);
        		}
        		
        		if (appointment.isFullday()) 
        		{
            		XmlUtil.setChildText(appointmentElem, "fullDay", "true");
            		XmlUtil.setChildText(appointmentElem, "fullDayNum", Integer.toString(appointment.getFullDayNum()));
            		XmlUtil.setChildText(appointmentElem, "fullDayTotalNum", Integer.toString(appointment.getFullDayTotalNum()));
            		XmlUtil.setChildText(appointmentElem, "fullDaysInCurrentMonth", Integer.toString(getFullDayNumInCurrentMonth(appointment)));
        		}
        	}
        }
	}
	
	private String replaceLineBreak(String origText)
	{
		if ((origText.indexOf('\n') < 0) && (origText.indexOf('\r') < 0)) 
		{
			return origText;
		}
		
		return (origText.replaceAll("\n", " ").replaceAll("\r", " "));
	}
	
	private int getFullDayNumInCurrentMonth(Appointment appointment) 
	{
		Calendar cal = new GregorianCalendar();
		cal.setTime(appointment.getEventTime());

		for (int i = 1; i < appointment.getFullDayNum(); i++) {
			cal.roll(Calendar.DAY_OF_MONTH, 1);
			if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
				return i;
			}
		}
		return appointment.getFullDayNum();
	}
	
}