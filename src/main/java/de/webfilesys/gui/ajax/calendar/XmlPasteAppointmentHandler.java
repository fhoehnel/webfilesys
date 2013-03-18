package de.webfilesys.gui.ajax.calendar;

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.calendar.Appointment;
import de.webfilesys.calendar.AppointmentManager;
import de.webfilesys.gui.ajax.XmlRequestHandlerBase;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

public class XmlPasteAppointmentHandler extends XmlRequestHandlerBase {
	
	public XmlPasteAppointmentHandler(
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
		if (!checkWriteAccess())
		{
			return;
		}

		String yearParam = req.getParameter("year");
		String monthParam = req.getParameter("month");
		String dayOfMonthParam = req.getParameter("dayOfMonth");
		
		if (CommonUtils.isEmpty(yearParam) || CommonUtils.isEmpty(monthParam) || CommonUtils.isEmpty(dayOfMonthParam))
		{
            Logger.getLogger(getClass()).warn("missing parameter");
			return;
		}
		
        int year = 0;
        int month = 0;
        int dayOfMonth = 0;
        try
        {
        	year = Integer.parseInt(yearParam);
        	month = Integer.parseInt(monthParam);
        	dayOfMonth = Integer.parseInt(dayOfMonthParam);
        }
        catch (NumberFormatException numEx)
        {
            Logger.getLogger(getClass()).warn("invalid parameter", numEx);
            return;
        }
        
		String appointmentId = (String) req.getSession(true).getAttribute(XmlMoveAppointmentHandler.SESSION_KEY_APPOINTMENT_TO_MOVE);

		req.getSession(true).removeAttribute(XmlMoveAppointmentHandler.SESSION_KEY_APPOINTMENT_TO_MOVE);
		
        if (CommonUtils.isEmpty(appointmentId))
        {
            Logger.getLogger(getClass()).warn("appointment id for move not found in session");
			return;
        }

        Appointment appointmentToMove = AppointmentManager.getInstance().getAppointment(uid, appointmentId);
		
        if (appointmentToMove == null) 
        {
            Logger.getLogger(getClass()).warn("appointment for move operation not found: " + appointmentId);
            return;
        }
        
        long alarmOffset = appointmentToMove.getEventTime().getTime() - appointmentToMove.getAlarmTime().getTime();
        
		Calendar cal = new GregorianCalendar(req.getLocale());
		
        cal.setTime(appointmentToMove.getEventTime());
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth + 1);
		
        appointmentToMove.setEventTime(cal.getTime());
        appointmentToMove.setAlarmTime(new Date(appointmentToMove.getEventTime().getTime() - alarmOffset));
        
        appointmentToMove.setAlarmed(false);
        appointmentToMove.setMailAlarmed(false);
        
        AppointmentManager.getInstance().updateAppointment(uid, appointmentToMove, true);
        
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "message", "appointment created");

		XmlUtil.setChildText(resultElement, "success", "true");
		
		doc.appendChild(resultElement);
		
		processResponse();
	}
}
