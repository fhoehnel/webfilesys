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

public class XmlCreateAppointmentHandler extends XmlRequestHandlerBase {
	public XmlCreateAppointmentHandler(
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

		String yearParam = getParameter("year");
		if (CommonUtils.isEmpty(yearParam)) {
			Logger.getLogger(getClass()).warn("missing parameter year");
			return;
		}
		String monthParam = getParameter("month");
		if (CommonUtils.isEmpty(monthParam)) {
			Logger.getLogger(getClass()).warn("missing parameter month");
			return;
		}
		String dayParam = getParameter("day");
		if (CommonUtils.isEmpty(dayParam)) {
			Logger.getLogger(getClass()).warn("missing parameter day");
			return;
		}
		
		int year;
		int month;
		int day;
		
		try
		{
			year = Integer.parseInt(yearParam);
			month = Integer.parseInt(monthParam);
			day = Integer.parseInt(dayParam);
		}
		catch (NumberFormatException numEx)
		{
			Logger.getLogger(getClass()).warn("invalid parameter", numEx);
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
		
		Appointment newAppointment = new Appointment();
		
		Calendar cal = GregorianCalendar.getInstance(req.getLocale());
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day + 1);
		
		cal.set(Calendar.HOUR_OF_DAY, startHour);
		cal.set(Calendar.MINUTE, startMinute);
		cal.set(Calendar.SECOND, 0);
		
		newAppointment.setEventTime(cal.getTime());

		long alarmAheadTime = (alarmAheadHours * 3600 * 1000) + (alarmAheadMinutes * 60 * 1000);
		newAppointment.setAlarmTime(new Date(cal.getTime().getTime() - alarmAheadTime));
		
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
		
		newAppointment.setDuration(duration);
		
		newAppointment.setSubject(subject);
		
		newAppointment.setContent(description);
		
		newAppointment.setRepeatPeriod(repeatPeriod);
		
		newAppointment.setAlarmType(alarmType);
		
		if (fullDayParam != null)
		{
		    newAppointment.setFullDay(true);
			if ((multiDayParam != null) && (fullDayNum > 0))
			{
				newAppointment.setFullDayNum(fullDayNum);
			}
		}
		
		AppointmentManager.getInstance().createAppointment(uid, newAppointment);
		
		Element resultElement = doc.createElement("result");
		
		XmlUtil.setChildText(resultElement, "message", "appointment created");

		XmlUtil.setChildText(resultElement, "success", "true");
		
		Element appointmentElem = doc.createElement("appointment");
		resultElement.appendChild(appointmentElem);
		
		XmlUtil.setChildText(appointmentElem, "id", newAppointment.getId());

		XmlUtil.setChildText(appointmentElem, "eventTime", Long.toString(newAppointment.getEventTime().getTime()));
		XmlUtil.setChildText(appointmentElem, "duration", Long.toString(newAppointment.getDuration()));
		
		cal.setTime(newAppointment.getEventTime());
		String formattedStartTime = String.format("%2d:%2d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
		XmlUtil.setChildText(appointmentElem, "startTime", formattedStartTime);
		
		int startTimeMinuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
		XmlUtil.setChildText(appointmentElem, "startMinuteOfDay", Integer.toString(startTimeMinuteOfDay));
		
		long appointmentEndTime = newAppointment.getEventTime().getTime() + newAppointment.getDuration();
		java.util.Date appointmentEndDate = new java.util.Date(appointmentEndTime);

		cal.setTime(appointmentEndDate);
		
		String formattedEndTime = String.format("%2d:%2d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
		XmlUtil.setChildText(appointmentElem, "endTime", formattedEndTime);

		int endTimeMinuteOfDay = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
		XmlUtil.setChildText(appointmentElem, "endMinuteOfDay", Integer.toString(endTimeMinuteOfDay));
		
		XmlUtil.setChildText(appointmentElem, "startHour", Integer.toString(newAppointment.getEventTime().getHours()));
		
		XmlUtil.setChildText(appointmentElem, "endHour", Integer.toString(appointmentEndDate.getHours()));

		XmlUtil.setChildText(appointmentElem, "fullDay", Boolean.toString(newAppointment.isFullday()));
		
		XmlUtil.setChildText(appointmentElem, "fullDayNum", Integer.toString(newAppointment.getFullDayNum()));
		
   		XmlUtil.setChildText(appointmentElem, "subject", newAppointment.getSubject(), true);

		XmlUtil.setChildText(appointmentElem, "repeatPeriod", Integer.toString(newAppointment.getRepeatPeriod()));
		XmlUtil.setChildText(appointmentElem, "alarmType", Integer.toString(newAppointment.getAlarmType()));

		XmlUtil.setChildText(appointmentElem, "alarmAheadHours", Integer.toString(alarmAheadHours));
		XmlUtil.setChildText(appointmentElem, "alarmAheadMinutes", Integer.toString(alarmAheadMinutes));
		
		if ((newAppointment.getContent() != null) && (newAppointment.getContent().length() > 0))
		{
    		XmlUtil.setChildText(appointmentElem, "description", newAppointment.getContent(), true);
		}
			
		doc.appendChild(resultElement);
		
		this.processResponse();
	}

}
