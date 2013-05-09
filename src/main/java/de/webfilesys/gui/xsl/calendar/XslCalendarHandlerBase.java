package de.webfilesys.gui.xsl.calendar;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import de.webfilesys.gui.xsl.XslRequestHandlerBase;

public class XslCalendarHandlerBase  extends XslRequestHandlerBase 
{
	public XslCalendarHandlerBase(
			HttpServletRequest req, 
    		HttpServletResponse resp,
            HttpSession session,
            PrintWriter output, 
            String uid)
	{
        super(req, resp, session, output, uid);
	}

	protected void addCalendarResources() 
	{
		addMsgResource("calendar.monday", getResource("calendar.monday","Monday"));
		addMsgResource("calendar.tuesday", getResource("calendar.tuesday","Tuesday"));
		addMsgResource("calendar.wednesday", getResource("calendar.wednesday","Wednesday"));
		addMsgResource("calendar.thursday", getResource("calendar.thursday","Thursday"));
		addMsgResource("calendar.friday", getResource("calendar.friday","Friday"));
		addMsgResource("calendar.saturday", getResource("calendar.saturday","Saturday"));
		addMsgResource("calendar.sunday", getResource("calendar.sunday","Sunday"));

		addMsgResource("calendar.january", getResource("calendar.january","January"));
		addMsgResource("calendar.february", getResource("calendar.february","February"));
		addMsgResource("calendar.march", getResource("calendar.march","March"));
		addMsgResource("calendar.april", getResource("calendar.april","April"));
		addMsgResource("calendar.may", getResource("calendar.may","May"));
		addMsgResource("calendar.june", getResource("calendar.june","June"));
		addMsgResource("calendar.july", getResource("calendar.july","July"));
		addMsgResource("calendar.august", getResource("calendar.august","August"));
		addMsgResource("calendar.september", getResource("calendar.september","September"));
		addMsgResource("calendar.october", getResource("calendar.october","Oktober"));
		addMsgResource("calendar.november", getResource("calendar.november","November"));
		addMsgResource("calendar.december", getResource("calendar.december","December"));

		addMsgResource("calendar.year", getResource("calendar.year","year"));
		addMsgResource("calendar.today", getResource("calendar.today","today"));
		addMsgResource("calendar.week", getResource("calendar.week","week"));
		addMsgResource("calendar.to", getResource("calendar.to","to"));
		
		addMsgResource("appointment.reminder", getResource("appointment.reminder","WebFileSys Reminder"));
		addMsgResource("appointment.reminderCloseButton", getResource("appointment.reminderCloseButton","OK"));

		addMsgResource("appointment.dontRemindAgain", getResource("appointment.dontRemindAgain","do not remind again"));
		addMsgResource("appointment.remindAgain", getResource("appointment.remindAgain","remind again in"));
		addMsgResource("appointment.minute", getResource("appointment.minute","minute"));
		addMsgResource("appointment.hour", getResource("appointment.hour","hour"));
		addMsgResource("appointment.day", getResource("appointment.day","day"));
		addMsgResource("appointment.week", getResource("appointment.week","week"));
	}
}
