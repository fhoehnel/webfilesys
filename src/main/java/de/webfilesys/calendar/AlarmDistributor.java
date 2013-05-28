package de.webfilesys.calendar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.log4j.Logger;

import de.webfilesys.LanguageManager;
import de.webfilesys.WebFileSys;
import de.webfilesys.mail.Email;

/**
 * @author Frank Hoehnel
 */
public class AlarmDistributor extends Thread 
{
	private static final long ALARM_CHECK_INTERVAL = 60000 * 1; 
	
	private int lastMailSentHourIdx = 0;
	private int mailsSentInLastHour = 0;
	
	public synchronized void run()
	{
    	Logger.getLogger(getClass()).info("AlarmDistributor started");

    	boolean shutdownFlag = false;

		while (!shutdownFlag)
		{
			try
			{
				this.wait(ALARM_CHECK_INTERVAL);

				Date gmtNow = new Date();
				
				checkAlarm(gmtNow);
			}
			catch (InterruptedException e)
			{
				shutdownFlag = true;
				if (Logger.getLogger(getClass()).isInfoEnabled())
				{
	            	Logger.getLogger(getClass()).info("AlarmDistributor ready for shutdown");
				}
			}
		}
	}

	protected void checkAlarm(Date gmtNow)
	{
    	ArrayList<AlarmEntry> mailAlarmList = AppointmentManager.getInstance().getAlarmIndex().getMailAlarmList(gmtNow);

		if (mailAlarmList != null)
		{
			for (int i = 0; i < mailAlarmList.size(); i++)
			{
				AlarmEntry entryToAlarm = mailAlarmList.get(i);

				StringBuffer tzone = new StringBuffer();
				tzone.append("(GMT");

				// int tzHourOffset = XmlUserProfileManager.getInstance().getTimeZone(entryToAlarm.owner);
				// TODO: fix this
				int tzHourOffset = 2;
				
				if (tzHourOffset >= 0)
				{
					tzone.append("+");
				}

				tzone.append(Integer.toString(tzHourOffset));
				tzone.append(")");

				Appointment appointment = AppointmentManager.getInstance().getAppointment(entryToAlarm.getOwner(), entryToAlarm.getXmlId());

				if (appointment != null)
				{
					if (WebFileSys.getInstance().getUserMgr().userExists(entryToAlarm.getOwner()))
					{
						if (sendAlarmMail(entryToAlarm.getOwner(), appointment, tzone.toString(), entryToAlarm.getEventDate()))
						{
							entryToAlarm.setMailAlarmed();
							entryToAlarm.setLastMailAlarmed(gmtNow);

							appointment.setMailAlarmed(true);
                            appointment.setLastMailAlarmed(gmtNow);
							
                            if (!entryToAlarm.isCloned())
                            {
    							setRepeatedAlarmTime(entryToAlarm, appointment);
                            }
                            else
                            {
                    			if (Logger.getLogger(getClass()).isDebugEnabled())
                    			{
                    				Logger.getLogger(getClass()).debug("ignoring alarm entry clone in repeat check: " + entryToAlarm);
                    			}
                            }

							AppointmentManager.getInstance().updateAppointment(entryToAlarm.getOwner(), appointment, false);
						}
					}
				}
			}   
		}
	}

    public boolean sendAlarmMail(String userid, Appointment appointment, String timezone)
    {
    	return sendAlarmMail(userid, appointment, timezone, null);
    }
    
    public boolean sendAlarmMail(String userid, Appointment appointment, String timezone, Date alarmTime)
    {
    	// this is an emergency brake to prevent sending uncontrolled numbers of mails if something unexpected happens
    	Calendar nowCal = new GregorianCalendar();
    	int currentHourIndex = nowCal.get(Calendar.DAY_OF_MONTH) * 100 + nowCal.get(Calendar.HOUR_OF_DAY);
    	if (currentHourIndex == lastMailSentHourIdx)
    	{
    		if (mailsSentInLastHour > WebFileSys.getInstance().getMaxAppointmentMailsPerHour())
    		{
            	Logger.getLogger(getClass()).warn("too many appointment mails sent per hour: " + mailsSentInLastHour);
                return false;
    		}
    		mailsSentInLastHour++;
    	}
    	else
    	{
        	lastMailSentHourIdx = currentHourIndex;
    		mailsSentInLastHour = 0;
    	}
    	
    	String language = WebFileSys.getInstance().getUserMgr().getLanguage(userid);
		SimpleDateFormat dateFormat = LanguageManager.getInstance().getDateFormat(language);

		String reminderSubjectPrefix = LanguageManager.getInstance().getResource(language, "calender.reminderSubjectPrefix", "WebFileSys Reminder");
		
		StringBuffer messageBody = new StringBuffer();
		
		if (alarmTime != null)
		{
			messageBody.append(dateFormat.format(alarmTime) + " " + timezone + "\r\n\r\n");
		}
		else
		{
			messageBody.append(dateFormat.format(appointment.getEventTime()) + " " + timezone + "\r\n\r\n");
		}
		
        messageBody.append(appointment.getContent());
        
		String receiver = WebFileSys.getInstance().getUserMgr().getEmail(userid);
		
		String mailSubject = reminderSubjectPrefix + ": " + appointment.getSubject();
		
		return (new Email(receiver, mailSubject, messageBody.toString())).sendSynchron();
    }
    
    /**
     * Moves the event time and alarm time of a repeated event to the next repeat time.
     * @param entryToAlarm the alarm index entry
     * @param appointment the appointment
     */
    private void setRepeatedAlarmTime(AlarmEntry entryToAlarm, Appointment appointment)
    {
    	if (entryToAlarm.getRepeatPeriod() == AlarmEntry.ALARM_NONE)
    	{
    		return;
    	}
    	
    	Date oldEventDate = entryToAlarm.getEventDate();
    	
        long alarmOffset = entryToAlarm.getEventDate().getTime() - entryToAlarm.getAlarmTime().getTime();
    	
    	if (entryToAlarm.getRepeatPeriod() == AlarmEntry.REPEAT_ANNUAL)
    	{
    		Calendar cal = new GregorianCalendar();
    		cal.setTime(entryToAlarm.getAlarmTime());
    		cal.add(Calendar.YEAR, 1);
    		entryToAlarm.setAlarmTime(new Date(cal.getTimeInMillis()));
    		entryToAlarm.setEventDate(new Date(entryToAlarm.getAlarmTime().getTime() + alarmOffset));
    	}
    	else if (entryToAlarm.getRepeatPeriod() == AlarmEntry.REPEAT_MONTHLY)
    	{
    		Calendar cal = new GregorianCalendar();
    		cal.setTime(entryToAlarm.getAlarmTime());
    		cal.add(Calendar.MONTH, 1);
    		entryToAlarm.setAlarmTime(new Date(cal.getTimeInMillis()));
    		entryToAlarm.setEventDate(new Date(entryToAlarm.getAlarmTime().getTime() + alarmOffset));
    	}
    	else
    	{
        	long nextAlarmOffset = 0;
        	if (entryToAlarm.getRepeatPeriod() == AlarmEntry.REPEAT_DAILY)
        	{
        		nextAlarmOffset = 1000 * 60 * 60 * 24;
        	}
        	else if (entryToAlarm.getRepeatPeriod() == AlarmEntry.REPEAT_WEEKLY)
        	{
        		nextAlarmOffset = 1000 * 60 * 60 * 24 * 7;
        	}
        	else if (entryToAlarm.getRepeatPeriod() == AlarmEntry.REPEAT_WEEKDAY)
        	{
        		Calendar cal = new GregorianCalendar();
        		cal.setTime(entryToAlarm.getAlarmTime());
        		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
        		{
            		nextAlarmOffset = 1000 * 60 * 60 * 24 * 3;
        		}
        		else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
        		{
            		nextAlarmOffset = 1000 * 60 * 60 * 24 * 2;
        		}
        		else
        		{
            		nextAlarmOffset = 1000 * 60 * 60 * 24;
        		}
        	}
        	
    		entryToAlarm.setAlarmTime(new Date(entryToAlarm.getAlarmTime().getTime() + nextAlarmOffset));
    		entryToAlarm.setEventDate(new Date(entryToAlarm.getAlarmTime().getTime() + alarmOffset));
    	}
    	
    	entryToAlarm.unsetMailAlarmed();
    	entryToAlarm.unsetAlarmed();

    	AppointmentManager.getInstance().getAlarmIndex().moveEvent(entryToAlarm, oldEventDate);    	
    }
}
