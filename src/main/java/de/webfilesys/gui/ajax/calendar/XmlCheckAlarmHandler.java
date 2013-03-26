package de.webfilesys.gui.ajax.calendar;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import de.webfilesys.LanguageManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.calendar.AlarmEntry;
import de.webfilesys.calendar.Appointment;
import de.webfilesys.calendar.AppointmentManager;
import de.webfilesys.gui.ajax.XmlRequestHandlerBase;
import de.webfilesys.util.XmlUtil;

public class XmlCheckAlarmHandler extends XmlRequestHandlerBase {
	public XmlCheckAlarmHandler(
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
		Calendar startOfDayCal = GregorianCalendar.getInstance(req.getLocale());

		startOfDayCal.set(Calendar.HOUR_OF_DAY, 0);
		startOfDayCal.set(Calendar.MINUTE, 0);
		startOfDayCal.set(Calendar.SECOND, 0);
		startOfDayCal.set(Calendar.MILLISECOND, 0);

		Calendar nowCal = GregorianCalendar.getInstance(req.getLocale());
		
		long nowTime = nowCal.getTimeInMillis();
		
		// catch the alarm one minute before the alarm distributor moves it to the next repeat period
		nowCal.setTimeInMillis(nowTime + 60000);
		
		List<AlarmEntry> alarmList = 
			AppointmentManager.getInstance().getAlarmIndex().getAlarmsForDateRange(uid, startOfDayCal.getTimeInMillis(), nowCal.getTimeInMillis());
		
		Element resultElement = doc.createElement("result");
		doc.appendChild(resultElement);

		String language = WebFileSys.getInstance().getUserMgr().getLanguage(uid);
		SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);
		
		ArrayList<AlarmEntry> clonesToRemove = new ArrayList<AlarmEntry>();
		
		Iterator<AlarmEntry> iter = alarmList.iterator();
		while (iter.hasNext()) {
			AlarmEntry alarmEntry = iter.next();
			
			if (!alarmEntry.isAlarmed())
			{
				if ((alarmEntry.getAlarmType() == AlarmEntry.ALARM_SOUND) ||
					    (alarmEntry.getAlarmType() == AlarmEntry.ALARM_VISUAL) ||
					    (alarmEntry.getAlarmType() == AlarmEntry.ALARM_ALL)) 
				{
					Appointment appointment = AppointmentManager.getInstance().getAppointment(uid, alarmEntry.getXmlId());

					if (appointment != null) 
					{
						Element alarmElement = doc.createElement("alarm");
						resultElement.appendChild(alarmElement);
						
						Calendar alarmCal = GregorianCalendar.getInstance(req.getLocale());
						
						alarmCal.setTime(alarmEntry.getAlarmTime());
						XmlUtil.setChildText(alarmElement, "alarmTime", dateFormat.format(alarmCal.getTime()));

						alarmCal.setTime(alarmEntry.getEventDate());
						XmlUtil.setChildText(alarmElement, "eventTime", dateFormat.format(alarmCal.getTime()));
						
						XmlUtil.setChildText(alarmElement, "subject", appointment.getSubject());

						XmlUtil.setChildText(alarmElement, "alarmType", Integer.toString(alarmEntry.getAlarmType()));
						
						XmlUtil.setChildText(alarmElement, "eventId", alarmEntry.getXmlId());

						appointment.setAlarmed(true);
						
						AppointmentManager.getInstance().updateAppointment(uid, appointment, true);
					}
					else
					{
				    	Logger.getLogger(getClass()).warn("Appointment not found with id " + alarmEntry.getXmlId());
					}
				}
			}
			
			if (alarmEntry.isCloned()) 
			{
				clonesToRemove.add(alarmEntry);
			}
		}
		
		for (int i = clonesToRemove.size() - 1; i >= 0; i--)
		{
			AlarmEntry clone = clonesToRemove.get(i);
			AppointmentManager.getInstance().getAlarmIndex().delEventClone(clone);
		}
		
		processResponse();
	}
}
