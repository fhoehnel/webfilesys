package de.webfilesys.calendar;

import java.util.*;

public class Appointment extends Note
{
    private Date eventTime;
    private Date alarmTime;
    
    private long duration;
    
    private int alarmType;
    
    private int repeatPeriod;
    
    private boolean repeatChecked;

    private boolean mailAlarmed;
    private boolean alarmed;

    private String copyReceiver;

    private long scheduleId;
    
    private Date lastMailAlarmed;
    
    private boolean fullDay = false;
    
    private int fullDayNum = 0;
    
    private int fullDayTotalNum = 0;

    public Appointment(String id)
    {
        super(id);

        init();
    }

    public Appointment()
    {
        super.init();

        init();
    }

    protected void init()
    {
        eventTime=null;
        alarmTime=null;
        duration=0L;
        alarmed=false;
        mailAlarmed=false;
        alarmType=AlarmEntry.ALARM_NONE;
        repeatPeriod=AlarmEntry.REPEAT_NONE;
        repeatChecked=false;
        copyReceiver=null;
        scheduleId=0L;
    }

    public Appointment getClone()
    {
        Appointment newAppointment=new Appointment();

        newAppointment.setSubject(this.getSubject());
        newAppointment.setContent(this.getContent());
        newAppointment.setEventTime(this.getEventTime());
        newAppointment.setAlarmTime(this.getAlarmTime());
        newAppointment.setDuration(this.getDuration());
        newAppointment.setFullDay(this.isFullday());
        newAppointment.setFullDayNum(this.getFullDayNum());
        newAppointment.setFullDayTotalNum(this.getFullDayTotalNum());
        newAppointment.setAlarmed(this.isAlarmed());
        newAppointment.setMailAlarmed(this.isMailAlarmed());
        newAppointment.setAlarmType(this.getAlarmType());
        newAppointment.setRepeatPeriod(this.getRepeatPeriod());
        newAppointment.setRepeatChecked(this.isRepeatChecked());
        newAppointment.setCopyReceiver(this.getCopyReceiver());
        newAppointment.setScheduleId(this.getScheduleId());
        newAppointment.setCreationTime(this.getCreationTime());
        newAppointment.setUpdateTime(new Date());

        return(newAppointment);
    }

    public void setEventTime(Date newEventTime)
    {
        eventTime=newEventTime;
    }

    public Date getEventTime()
    {
        return(eventTime);
    }

    public void setAlarmTime(Date newAlarmTime)
    {
        alarmTime=newAlarmTime;
    }

    public Date getAlarmTime()
    {
        return(alarmTime);
    }

    public void setAlarmed(boolean isAlarmed)
    {
        alarmed=isAlarmed;
    }

    public boolean isAlarmed()
    {
        return(alarmed);
    }

    public void setMailAlarmed(boolean alarmed)
    {
        mailAlarmed=alarmed;
    }

    public boolean isMailAlarmed()
    {
        return(mailAlarmed);
    }

    public void setAlarmType(int newAlarmType)
    {
        alarmType=newAlarmType;
    }

    public int getAlarmType()
    {
        return(alarmType);
    }

    public void setDuration(long newDuration)
    {
        duration=newDuration;
    }

    public long getDuration()
    {
        return(duration);
    }

    public void setRepeatPeriod(int newRepeatPeriod)
    {
        repeatPeriod=newRepeatPeriod;
    }

    public int getRepeatPeriod()
    {
        return(repeatPeriod);
    }

    public void setRepeatChecked(boolean checked)
    {
        repeatChecked=checked;
    }

    public boolean isRepeatChecked()
    {
        return(repeatChecked);
    }

    public void setCopyReceiver(String newCopyReceiver)
    {
        copyReceiver=newCopyReceiver;
    }

    public String getCopyReceiver()
    {
        return(copyReceiver);
    }

    public void setScheduleId(long newId)
    {
        scheduleId=newId;
    }

    public long getScheduleId()
    {
        return(scheduleId);
    }
    
    public Date getLastMailAlarmed()
    {
    	return lastMailAlarmed;
    }
    
    public void setLastMailAlarmed(Date newVal)
    {
    	lastMailAlarmed = newVal;
    }
    
    public void setFullDay(boolean newVal) {
    	fullDay = newVal;
    }
    
    public boolean isFullday() {
    	return fullDay;
    }
    
    public void setFullDayNum(int newVal) {
    	fullDayNum = newVal;
    }
    
    /** 
     * For cross-month appointments clones of the original appointment have different values for fullDayNum. 
     * @return number of full days for multi-day events
     */
    public int getFullDayNum() {
    	return fullDayNum;
    }

    public void setFullDayTotalNum(int newVal) {
    	fullDayTotalNum = newVal;
    }
    
    /** 
     * For cross-month appointments clones of the original appointment have the same value for fullDayTotalNum. 
     * @return number of full days for multi-day events
     */
    public int getFullDayTotalNum() {
    	if (fullDayTotalNum > 0) {
    		return fullDayTotalNum;
    	}
    	return fullDayNum;
    }
}
