package de.webfilesys.calendar;

import java.io.Serializable;
import java.util.Date;

public class AlarmEntry implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public static final int ALARM_NONE=0;
    public static final int ALARM_VISUAL=1;
    public static final int ALARM_SOUND=2;
    public static final int ALARM_MAIL=3;
    public static final int ALARM_ALL=4;

    public static final int ALARM_CMD=10;
    public static final int ALARM_VIDEO=11;

    public static final int ALARM_NOTEPAD=20;

    public static final int REPEAT_NONE=0;
    public static final int REPEAT_DAILY=1;
    public static final int REPEAT_WEEKLY=2;
    public static final int REPEAT_MONTHLY=3;
    public static final int REPEAT_ANNUAL=4;
    public static final int REPEAT_WEEKDAY=5;

    private Date eventDate;
    private Date alarmTime;
    
    private Date lastMailAlarmed;

	private int alarmType;
	private long dateId;
	private String owner;
	private boolean alarmed;
	private boolean mailAlarmed;
	private int repeatPeriod;
	private boolean cloned;

    private String xmlId;

    public AlarmEntry(long dateId,String owner,Date eventDate,Date alarmTime,
                     int alarmType,int repeatPeriod)
    {
        this.dateId=dateId;
        this.owner=owner;
        this.eventDate=eventDate;
        this.alarmTime=alarmTime;

        this.alarmType=alarmType;
        this.repeatPeriod=repeatPeriod;
        alarmed=false;
        mailAlarmed=false;
    }    

    public void setXmlId(String newXmlId)
    {
        xmlId=newXmlId;
    }

    public String getXmlId()
    {
        return(xmlId);
    }

    public void setDateId(long newVal)
    {
    	dateId = newVal;
    }
    
    public long getDateId()
    {
    	return dateId;
    }
    
    public void setOwner(String newVal)
    {
    	owner = newVal;
    }
    
    public String getOwner()
    {
    	return owner;
    }
    
    public void setEventDate(Date newVal)
    {
    	eventDate = newVal;
    }
    
    public Date getEventDate()
    {
    	return eventDate;
    }
    
    public void setAlarmTime(Date newVal)
    {
    	alarmTime = newVal;
    }
    
    public Date getAlarmTime()
    {
    	return alarmTime;
    }
    
    public void setAlarmType(int newVal)
    {
    	alarmType = newVal;
    }
    
    public int getAlarmType()
    {
    	return alarmType;
    }
    
    public void setRepeatPeriod(int newVal)
    {
    	repeatPeriod = newVal;
    }
 
    public int getRepeatPeriod()
    {
    	return repeatPeriod;
    }
    
    public boolean isAlarmed()
    {
        return(alarmed);
    }

    public void setAlarmed()
    {
        alarmed = true;
    }

    public void unsetAlarmed()
    {
        alarmed = false;
    }

    public boolean isMailAlarmed()
    {
        return(mailAlarmed);
    }

    public void setMailAlarmed()
    {
        mailAlarmed = true;
    }

    public void unsetMailAlarmed()
    {
        mailAlarmed = false;
    }

    public Date getLastMailAlarmed()
    {
    	return lastMailAlarmed;
    }
    
    public void setLastMailAlarmed(Date newVal)
    {
    	lastMailAlarmed = newVal;
    }
    
    public void setCloned(boolean newVal) 
    {
    	cloned = newVal;
    }
    
    public boolean isCloned() 
    {
    	return cloned;
    }
    
    public static String alarmTypeString(int alarmType)
    {
        if (alarmType==AlarmEntry.ALARM_VISUAL)
            return("Visual");
        if (alarmType==AlarmEntry.ALARM_SOUND)
            return("Sound");
        if (alarmType==AlarmEntry.ALARM_MAIL)
            return("e-mail");
        if (alarmType==AlarmEntry.ALARM_ALL)
            return("all");
        return("none");
    }
    
    public String toString()
    {
    	StringBuffer buff = new StringBuffer();
    	buff.append("AlarmEntry[");
    	buff.append("xmlId=");
    	buff.append(getXmlId());
    	buff.append(",owner=");
    	buff.append(getOwner());
    	buff.append(",dateId=");
    	buff.append(getDateId());
    	buff.append(",eventDate=");
    	buff.append(getEventDate());
    	buff.append(",alarmTime=");
    	buff.append(getAlarmTime());
    	buff.append(",alarmType=");
    	buff.append(getAlarmType());
    	buff.append(",repeatPeriod=");
    	buff.append(getRepeatPeriod());
    	buff.append(",alarmed=");
    	buff.append(isAlarmed());
    	buff.append(",mailAlarmed=");
    	buff.append(isMailAlarmed());
    	buff.append(",cloned=");
    	buff.append(isCloned());
    	buff.append("]");
    	return buff.toString();
    }
}