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

public class XmlChangeAppointmentHandler extends XmlRequestHandlerBase {
	public XmlChangeAppointmentHandler(
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

		String appointmentId = getParameter("appointmentId");
		
		if (CommonUtils.isEmpty(appointmentId))
		{
			Logger.getLogger(getClass()).warn("missing parameter appointmentId");
			return;
		}
		
		Appointment appointment = AppointmentManager.getInstance().getAppointment(uid, appointmentId);
		if (appointment == null)
		{
			Logger.getLogger(getClass()).warn("appointment for update not found with id " + appointmentId);
			return;
		}
		
		String subject = getParameter("subject");
		if (CommonUtils.isEmpty(subject)) {
			subject = "no subject";
		}

		String description = getParameter("description");

		String startHourParam = getParameter("startHour");
		if (CommonUtils.isEmpty(startHourParam)) {
			Logger.getLogger(getClass()).warn("missing parameter startHour");
			return;
		}
		String startMinuteParam = getParameter("startMinute");
		if (CommonUtils.isEmpty(startMinuteParam)) {
			Logger.getLogger(getClass()).warn("missing parameter startMinute");
			return;
		}

		String endHourParam = getParameter("endHour");
		if (CommonUtils.isEmpty(endHourParam)) {
			Logger.getLogger(getClass()).warn("missing parameter endHour");
			return;
		}
		String endMinuteParam = getParameter("endMinute");
		if (CommonUtils.isEmpty(endMinuteParam)) {
			Logger.getLogger(getClass()).warn("missing parameter endMinute");
			return;
		}
		
		String fullDayParam = getParameter("fullDay");
		
		String multiDayParam = getParameter("multiDay");
		
		String fullDayNumParam = getParameter("numOfDays");
		
		String repeatPeriodParam = getParameter("repeatPeriod");
		if (CommonUtils.isEmpty(repeatPeriodParam)) {
			Logger.getLogger(getClass()).warn("missing parameter repeatPeriod");
			return;
		}
		
		String alarmTypeParam = getParameter("alarmType");
		if (CommonUtils.isEmpty(alarmTypeParam)) {
			Logger.getLogger(getClass()).warn("missing parameter alarmType");
			return;
		}
		
		String alarmAheadHoursParam = getParameter("alarmAheadHours");
		if (CommonUtils.isEmpty(alarmAheadHoursParam)) {
			alarmAheadHoursParam = "0";
		}
		String alarmAheadMinutesParam = getParameter("alarmAheadMinutes");
		if (CommonUtils.isEmpty(alarmAheadMinutesParam)) {
			alarmAheadMinutesParam = "0";
		}
		
		int startHour;
		int endHour;
		int startMinute;
		int endMinute;
		int repeatPeriod;
		int alarmType;
		int alarmAheadHours;
		int alarmAheadMinutes;
		int fullDayNum = 0;
		
		try
		{
			startHour = Integer.parseInt(startHourParam);
			startMinute = Integer.parseInt(startMinuteParam);
			endHour = Integer.parseInt(endHourParam);
			endMinute = Integer.parseInt(endMinuteParam);
			repeatPeriod = Integer.parseInt(repeatPeriodParam);
			alarmType = Integer.parseInt(alarmTypeParam);
			alarmAheadHours = Integer.parseInt(alarmAheadHoursParam);
			alarmAheadMinutes = Integer.parseInt(alarmAheadMinutesParam);
			if (!CommonUtils.isEmpty(fullDayNumParam))
			{
				fullDayNum = Integer.parseInt(fullDayNumParam);
			}
		}
		catch (NumberFormatException numEx)
		{
			Logger.getLogger(getClass()).warn("invalid parameter value", numEx);
			return;
		}
		
		Date oldEventTime = appointment.getEventTime();
		
		Calendar cal = GregorianCalendar.getInstance(req.getLocale());
		cal.setTime(appointment.getEventTime());
		
		cal.set(Calendar.HOUR_OF_DAY, startHour);
		cal.set(Calendar.MINUTE, startMinute);
		cal.set(Calendar.SECOND, 0);
		
		appointment.setEventTime(cal.getTime());

		if (appointment.getEventTime().getTime() > oldEventTime.getTime())
		{
			appointment.setAlarmed(false);
			appointment.setMailAlarmed(false);
			appointment.setLastMailAlarmed(oldEventTime);
		}
		
		long alarmAheadTime = (alarmAheadHours * 3600 * 1000) + (alarmAheadMinutes * 60 * 1000);
		appointment.setAlarmTime(new Date(cal.getTime().getTime() - alarmAheadTime));
		
		long startTime = cal.getTime().getTime();
		
		if (fullDayParam != null)
		{
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
		}
		else 
		{
		    cal.set(Calendar.HOUR_OF_DAY, endHour);
		    cal.set(Calendar.MINUTE, endMinute);
		}
		
		long endTime = cal.getTime().getTime();
		
		long duration = endTime - startTime;
		
		appointment.setDuration(duration);
		
		appointment.setSubject(subject);
		
		appointment.setContent(description);
		
		appointment.setRepeatPeriod(repeatPeriod);
		
		appointment.setAlarmType(alarmType);
		
		if (fullDayParam != null)
		{
			appointment.setFullDay(true);
			if ((multiDayParam != null) && (fullDayNum > 0))
			{
				appointment.setFullDayNum(fullDayNum);
			}
			else
			{
				appointment.setFullDayNum(0);
			}
		}
		else 
		{
			appointment.setFullDay(false);
		}
		
		AppointmentManager.getInstance().updateAppointment(uid, appointment, true);
		
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "message", "appointment created");

		XmlUtil.setChildText(resultElement, "success", "true");
		
		Element appointmentElem = doc.createElement("appointment");
		resultElement.appendChild(appointmentElem);
		
		XmlUtil.setChildText(appointmentElem, "id", appointment.getId());

		XmlUtil.setChildText(appointmentElem, "eventTime", Long.toString(appointment.getEventTime().getTime()));
		XmlUtil.setChildText(appointmentElem, "duration", Long.toString(appointment.getDuration()));
		
		cal.setTime(appointment.getEventTime());
		String formattedStartTime = String.format("%2d:%2d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
		XmlUtil.setChildText(appointmentElem, "startTime", formattedStartTime);
		
		int startTimeMinuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
		XmlUtil.setChildText(appointmentElem, "startMinuteOfDay", Integer.toString(startTimeMinuteOfDay));
		
		long appointmentEndTime = appointment.getEventTime().getTime() + appointment.getDuration();
		java.util.Date appointmentEndDate = new java.util.Date(appointmentEndTime);

		cal.setTime(appointmentEndDate);
		
		String formattedEndTime = String.format("%2d:%2d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
		XmlUtil.setChildText(appointmentElem, "endTime", formattedEndTime);

		int endTimeMinuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
		XmlUtil.setChildText(appointmentElem, "endMinuteOfDay", Integer.toString(endTimeMinuteOfDay));
		
		XmlUtil.setChildText(appointmentElem, "startHour", Integer.toString(appointment.getEventTime().getHours()));
		
		XmlUtil.setChildText(appointmentElem, "endHour", Integer.toString(appointmentEndDate.getHours()));

		XmlUtil.setChildText(appointmentElem, "fullDay", Boolean.toString(appointment.isFullday()));
		
		XmlUtil.setChildText(appointmentElem, "fullDayNum", Integer.toString(appointment.getFullDayNum()));
		
   		XmlUtil.setChildText(appointmentElem, "subject", appointment.getSubject(), true);

		XmlUtil.setChildText(appointmentElem, "repeatPeriod", Integer.toString(appointment.getRepeatPeriod()));
		XmlUtil.setChildText(appointmentElem, "alarmType", Integer.toString(appointment.getAlarmType()));

		XmlUtil.setChildText(appointmentElem, "alarmAheadHours", Integer.toString(alarmAheadHours));
		XmlUtil.setChildText(appointmentElem, "alarmAheadMinutes", Integer.toString(alarmAheadMinutes));
		
		if ((appointment.getContent() != null) && (appointment.getContent().length() > 0))
		{
    		XmlUtil.setChildText(appointmentElem, "description", appointment.getContent(), true);
		}
			
		doc.appendChild(resultElement);
		
		this.processResponse();
	}

}
