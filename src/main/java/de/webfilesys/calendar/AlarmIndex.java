package de.webfilesys.calendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

public class AlarmIndex extends Hashtable<String, Hashtable<Integer, Vector<AlarmEntry>>> implements Serializable
{
	private static final long serialVersionUID = 1L;
	
    // provides a unique id for the alarm entries
    protected long idCounter;

    public AlarmIndex()
    {
        super();
        idCounter = 1;
    }

    private Integer getDateKey(Date eventDate)
    {
        Calendar cal = new GregorianCalendar();
        cal.setTime(eventDate);

        int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int year = cal.get(Calendar.YEAR);
        Integer dateKey = new Integer((year*1000) + dayOfYear);      
        return dateKey;
    }

    public synchronized AlarmEntry addEvent(String owner, Appointment appointment)
    {
    	return addEvent(appointment.getId(), owner, appointment.getEventTime(), appointment.getAlarmTime(),
                        appointment.getAlarmType(), appointment.getRepeatPeriod(),
                        appointment.isAlarmed(), appointment.isMailAlarmed());    	
    }
    
    public synchronized AlarmEntry addEvent(String xmlId, String owner, Date newDate, Date alarmTime,
                                           int alarmType, int repeatPeriod, 
                                           boolean alarmed, boolean mailAlarmed)
    {
        Integer dateKey = getDateKey(newDate);        
        
        Hashtable<Integer, Vector<AlarmEntry>> userRoot = (Hashtable<Integer, Vector<AlarmEntry>>) get(owner);
        if (userRoot==null)
        {
            userRoot = new Hashtable<Integer, Vector<AlarmEntry>>();
            put(owner,userRoot);
        }

        Vector<AlarmEntry> dayEventList = (Vector<AlarmEntry>) userRoot.get(dateKey);
        if(dayEventList == null)
        {
            dayEventList = new Vector<AlarmEntry>();
            userRoot.put(dateKey,dayEventList);
        }

        AlarmEntry newEvent = new AlarmEntry(idCounter++, owner, newDate, alarmTime, alarmType, repeatPeriod);

        newEvent.setXmlId(xmlId);
        
        if (alarmed)
        {
            newEvent.setAlarmed();
        }
        if (mailAlarmed)
        {
            newEvent.setMailAlarmed();
        }

        boolean stop = false;
        synchronized (dayEventList)
        {
            for(int i = 0; (!stop) && (i < dayEventList.size()); i++)
            {
                AlarmEntry actEntry = dayEventList.elementAt(i);
                if(actEntry.getEventDate().getTime() > newDate.getTime())
                {
                    dayEventList.insertElementAt(newEvent, i);
                    stop = true;
                }
            }
            if (!stop)
            {
                dayEventList.addElement(newEvent);
            }
        }

		if (Logger.getLogger(getClass()).isDebugEnabled())
		{
			Logger.getLogger(getClass()).debug("adding AlarmEntry to alarm index: " + newEvent);
		}

		return(newEvent);
    }

    public boolean delEventClone(AlarmEntry entryToRemove)
    {
		Hashtable<Integer, Vector<AlarmEntry>> userRoot = get(entryToRemove.getOwner());

		if (userRoot == null)
		{
			return false;
		}
		
        Integer dateKey = getDateKey(entryToRemove.getEventDate());        
		
		Vector<AlarmEntry> dayEventList = userRoot.get(dateKey);
		if (dayEventList == null)
		{
			return false;
		}

		Enumeration<AlarmEntry> allEvents = dayEventList.elements();
		
		while (allEvents.hasMoreElements())
		{
			AlarmEntry nextEvent = allEvents.nextElement();
			
			if (nextEvent.getXmlId().equals(entryToRemove.getXmlId()))
			{
				if (nextEvent.isCloned())
				{
					if (Logger.getLogger(getClass()).isDebugEnabled())
					{
						Logger.getLogger(getClass()).debug("removing AlarmEntry clone from alarm index: " + nextEvent);
					}
					
					dayEventList.remove(nextEvent);

					return(true);
				}
			}
		}
		
		return false;
    }

	public boolean delEvent(String owner, Appointment appointment)
	{
		Hashtable<Integer, Vector<AlarmEntry>> userRoot = get(owner);

		if (userRoot == null)
		{
			return(false);
		}
		
		if (delEvent(appointment.getId(), appointment.getEventTime(), userRoot))
		{
			return true;
		}
		if (delEvent(appointment.getId(), new Date(), userRoot))
		{
			// check index for today
			return true;
		}
		
		// check index for tomorrow
		return delEvent(appointment.getId(), new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000l)), userRoot);
	}		

	public boolean delEvent(String eventXmlId, Date eventDate, Hashtable<Integer, Vector<AlarmEntry>> userRoot)
	{
        Integer dateKey = getDateKey(eventDate);        
		
		Vector<AlarmEntry> dayEventList = userRoot.get(dateKey);
		if (dayEventList == null)
		{
			return false;
		}

		Enumeration<AlarmEntry> allEvents = dayEventList.elements();
		
		while (allEvents.hasMoreElements())
		{
			AlarmEntry nextEvent = allEvents.nextElement();
			
			if (nextEvent.getXmlId().equals(eventXmlId))
			{
				dayEventList.remove(nextEvent);

				if (Logger.getLogger(getClass()).isDebugEnabled())
				{
					Logger.getLogger(getClass()).debug("removing AlarmEntry from alarm index " + nextEvent);
				}
				
				return(true);
			}
		}
		
		return(false);
	}
	
	public void moveEvent(AlarmEntry entryToMove, Date oldEventTime)
	{
		String owner = entryToMove.getOwner();	
		
        Integer dateKey = getDateKey(oldEventTime);        
		
		Hashtable<Integer, Vector<AlarmEntry>> userRoot = get(owner);

		if (userRoot == null)
		{
	    	Logger.getLogger(getClass()).warn("AlarmEntry to move for user " + owner + " not found");
			return;
		}

		Vector<AlarmEntry> dayEventList = userRoot.get(dateKey);
		if (dayEventList == null)
		{
	    	Logger.getLogger(getClass()).warn("day event list for user " + owner + " not found");
			return;
		}

		if (!dayEventList.remove(entryToMove))
		{
	    	Logger.getLogger(getClass()).warn("AlarmEntry to remove not found");
		}
		
		Integer newDateKey = getDateKey(entryToMove.getEventDate());
		
		dayEventList = userRoot.get(newDateKey);
		if (dayEventList == null)
		{
			dayEventList = new Vector<AlarmEntry>();
			userRoot.put(newDateKey, dayEventList);
		}
			
		dayEventList.add(entryToMove);

		if (Logger.getLogger(getClass()).isDebugEnabled())
		{
			Logger.getLogger(getClass()).debug("AlarmEntry moved from " + oldEventTime + " to " + entryToMove.getEventDate());
		}
	}
    
	public List<AlarmEntry> getAlarmsForDateRange(String userid, long startTime, long endTime)
	{
		ArrayList<AlarmEntry> alarmList = new ArrayList<AlarmEntry>();
		
        Vector<AlarmEntry> dayEventList = getDayEventVector(userid, new Date(startTime));
        
        if (dayEventList != null) 
        {
            Iterator<AlarmEntry> iter = dayEventList.iterator();
            
            while (iter.hasNext()) 
            {
            	AlarmEntry alarm = iter.next();
            	
            	if ((alarm.getAlarmTime().getTime() >= startTime) && (alarm.getAlarmTime().getTime() <= endTime))
            	{
            		alarmList.add(alarm);
            	}
            }
        }
        
        return alarmList;
	}
	
    public ArrayList<AlarmEntry> getVisualAlarmList(String userid)
    {
    	ArrayList<AlarmEntry> visualAlarmVector = null;

        Date today = new Date();
        long now = today.getTime();

        Vector<AlarmEntry> dayEventList = getDayEventVector(userid, today);

        if (dayEventList != null)
        {
            synchronized (dayEventList)
            {
            	for(int i = 0; i < dayEventList.size(); i++)
                {
            		AlarmEntry nextEvent = dayEventList.elementAt(i);
                    if((!nextEvent.isAlarmed()) &&
                       ((nextEvent.getAlarmType() == AlarmEntry.ALARM_VISUAL) ||
                        (nextEvent.getAlarmType() == AlarmEntry.ALARM_SOUND) ||
                        (nextEvent.getAlarmType() == AlarmEntry.ALARM_ALL)) &&
                       (nextEvent.getAlarmTime().getTime() < now))
                    {
                        if(visualAlarmVector==null)
                        {
                            visualAlarmVector = new ArrayList<AlarmEntry>();
                        }
                        visualAlarmVector.add(nextEvent);
                    }
                }
            }
        }

      // check also for tomorrow

        Date tomorrow = new Date(now + 86400000);

        dayEventList = getDayEventVector(userid, tomorrow);

        if (dayEventList!= null)
        {
            synchronized (dayEventList)
            {
            	for(int i = 0; i < dayEventList.size(); i++)
                {
            		AlarmEntry nextEvent = dayEventList.elementAt(i);
                    if((!nextEvent.isAlarmed()) &&
                       ((nextEvent.getAlarmType() == AlarmEntry.ALARM_VISUAL) ||
                        (nextEvent.getAlarmType() == AlarmEntry.ALARM_SOUND) ||
                        (nextEvent.getAlarmType() == AlarmEntry.ALARM_ALL)) &&
                       (nextEvent.getAlarmTime().getTime() < now))
                    {
                        if (visualAlarmVector == null)
                        {
                            visualAlarmVector = new ArrayList<AlarmEntry>();
                        }
                        visualAlarmVector.add(nextEvent);
                    }
                }
            }
        }

        return(visualAlarmVector);
    }

    public Vector<AlarmEntry> getDayEventVector(String owner, Date searchDate)
    {
        Integer dateKey = getDateKey(searchDate);        
        
		Hashtable<Integer, Vector<AlarmEntry>> userRoot = get(owner);

		if (userRoot == null)
		{
			return(null);
		}

        Vector<AlarmEntry> dayEventList = userRoot.get(dateKey);

        return(dayEventList);
    }

    public ArrayList<AlarmEntry> getMailAlarmList(Date today)
    {
    	ArrayList<AlarmEntry> mailAlarmList = null;

        long now = today.getTime();

        Enumeration<String> allUsers = keys();

        while (allUsers.hasMoreElements())
        {
            String actUser = allUsers.nextElement();

            Vector<AlarmEntry> dayEventList = getDayEventVector(actUser, today);
        
            if (dayEventList != null)
            {
                synchronized (dayEventList)
                {
                    for (int i = 0; i < dayEventList.size(); i++)
                    {
                    	AlarmEntry nextEvent = dayEventList.elementAt(i);
                        if ((!nextEvent.isMailAlarmed()) &&
                            ((nextEvent.getAlarmType() == AlarmEntry.ALARM_MAIL) ||
                             (nextEvent.getAlarmType() == AlarmEntry.ALARM_ALL)) &&
                            (nextEvent.getAlarmTime().getTime() < now))
                        {
                            if (mailAlarmList == null)
                            {
                                mailAlarmList = new ArrayList<AlarmEntry>();
                            }
                            mailAlarmList.add(nextEvent);
                        }
                    }
                }
            }

          // check also for tomorrow

            Date tomorrow = new Date(now + 86400000);

            dayEventList = getDayEventVector(actUser, tomorrow);

            if (dayEventList!=null)
            {
                synchronized (dayEventList)
                {
                    for(int i = 0; i < dayEventList.size(); i++)
                    {
                        AlarmEntry nextEvent = dayEventList.elementAt(i);
                        if((!nextEvent.isMailAlarmed()) &&
                           ((nextEvent.getAlarmType() == AlarmEntry.ALARM_MAIL) ||
                            (nextEvent.getAlarmType() == AlarmEntry.ALARM_ALL)) &&
                           (nextEvent.getAlarmTime().getTime() < now))
                        {
                            if (mailAlarmList == null)
                            {
                                mailAlarmList = new ArrayList<AlarmEntry>();
                            }
                            mailAlarmList.add(nextEvent);
                        }
                    }
                }
            }
        }

        return(mailAlarmList);
    }

    public ArrayList<AlarmEntry> getSchedList(Date today)
    {
    	ArrayList<AlarmEntry> schedVector = null;

        long now = today.getTime();

        Enumeration<String> allUsers = keys();

        while (allUsers.hasMoreElements())
        {
            String actUser=(String) allUsers.nextElement();

            Vector<AlarmEntry> dayEventList = getDayEventVector(actUser, today);
        
            if (dayEventList != null)
            {
                synchronized (dayEventList)
                { 
                    for(int i = 0; i < dayEventList.size(); i++)
                    {
                    	AlarmEntry nextEvent = dayEventList.elementAt(i);
                        if ((!nextEvent.isMailAlarmed()) &&
                            ((nextEvent.getAlarmType() == AlarmEntry.ALARM_CMD) ||
                             (nextEvent.getAlarmType() == AlarmEntry.ALARM_VIDEO)) &&
                            (nextEvent.getAlarmTime().getTime() < now) &&
                            (nextEvent.getAlarmTime().getTime() > (now-300000)))
                        {
                            if (schedVector == null)
                            {
                                schedVector = new ArrayList<AlarmEntry>();
                            }
                            schedVector.add(nextEvent);
                        }
                    }
                }
            }

          // check also for tomorrow

            Date tomorrow = new Date(now + 86400000);

            dayEventList = getDayEventVector(actUser, tomorrow);

            if (dayEventList != null)
            {
                synchronized (dayEventList)
                {
                    for(int i = 0; i < dayEventList.size(); i++)
                    {
                    	AlarmEntry nextEvent = dayEventList.elementAt(i);
                        if ((!nextEvent.isMailAlarmed()) &&
                            ((nextEvent.getAlarmType() == AlarmEntry.ALARM_CMD) ||
                             (nextEvent.getAlarmType() == AlarmEntry.ALARM_VIDEO)) &&
                            (nextEvent.getAlarmTime().getTime() < now) &&
                            (nextEvent.getAlarmTime().getTime() > (now-300000)))
                        {
                            if (schedVector == null)
                            {
                                schedVector = new ArrayList<AlarmEntry>();
                            }
                            schedVector.add(nextEvent);
                        }
                    }
                }
            }
        }

        return(schedVector);
    }

    public boolean setAlarmed(String owner, Date searchDate, long searchedId)
    {
        AlarmEntry foundEntry = findDateEntry(owner, searchDate, searchedId);
        if (foundEntry != null)
        {
        	foundEntry.setAlarmed();
            return true;
        }
        
        return(false);
    }

    public boolean setMailAlarmed(String owner, Date searchDate, long searchedId)
    {
        AlarmEntry foundEntry = findDateEntry(owner, searchDate, searchedId);
        if (foundEntry != null)
        {
        	foundEntry.setMailAlarmed();
            return true;
        }
        
        return(false);
    }

    private AlarmEntry findDateEntry(String owner, Date searchDate, long searchedId)
    {
        Vector<AlarmEntry> dayEventList = getDayEventVector(owner, searchDate);
        if (dayEventList == null)
        {
            return(null);
        }
        
        synchronized (dayEventList)
        {
        	for(int i = 0; i < dayEventList.size(); i++)
            {
                AlarmEntry nextEvent = dayEventList.elementAt(i);
                if (nextEvent.getDateId() == searchedId)
                {
                    return(nextEvent);
                }
            }
        }

        return(null);
    }
    
    public void deleteUserEntries(String userid)
    {
        if (get(userid) != null)
        {
            remove(userid);
        }
    }

}


