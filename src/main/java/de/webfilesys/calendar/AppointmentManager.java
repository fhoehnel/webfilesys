package de.webfilesys.calendar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.webfilesys.WebFileSys;
import de.webfilesys.util.CommonUtils;
import de.webfilesys.util.XmlUtil;

public class AppointmentManager extends Thread
{
    private String appointmentDir = null;

    HashMap<String, Element> appointmentMap = null;

    HashMap<String, HashMap<String, Element>> indexTable = null;

    HashMap<String, Boolean> cacheDirty=null;

    DocumentBuilder builder = null;

    private static AppointmentManager appointmentManager=null;
    
    AlarmIndex alarmIdx = null;
    
    AlarmDistributor alarmDistributor = null;

    private AppointmentManager()
    {
        appointmentDir = WebFileSys.getInstance().getConfigBaseDir() + "/" + "appointments";

        appointmentMap = new HashMap<String, Element>();
        indexTable = new HashMap<String, HashMap<String, Element>>();
        
        cacheDirty = new HashMap<String, Boolean>();

        builder = null;

        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
        }
        catch (ParserConfigurationException pcex)
        {
        	Logger.getLogger(getClass()).error(pcex);
        }

        alarmDistributor = new AlarmDistributor();

        loadAlarmIndexFromXml();
        
        saveChangedUsers();
        
        alarmDistributor.start();
        
        start();
    }

    public synchronized static AppointmentManager getInstance()
    {
        if (appointmentManager == null)
        {
            appointmentManager = new AppointmentManager();
        }

        return(appointmentManager);
    }

    public synchronized void run()
    {
    	Logger.getLogger(getClass()).info("AppointmentManager started");

    	boolean stop = false;
    	
    	int loopCounter = 0;
    	
        while (!stop)
        {
        	loopCounter++;
            try
            {
                this.wait(60000);
                saveChangedUsers();

                if (loopCounter == 60) 
            	{
            		synchronized (appointmentMap)
            		{
                		synchronized (indexTable)
                		{
                    		synchronized (cacheDirty)
                    		{
                    			Date now = new Date();
                    			if (now.getHours() == 0)
                    			{
                    				expireUnrepeatedEvents();
                    			}
                    			
                    			if (Logger.getLogger(getClass()).isDebugEnabled()) {
                                	Logger.getLogger(getClass()).debug("AppointmentManager clearing appointment cache");
                    			}
                                appointmentMap.clear();
                                indexTable.clear();
                                cacheDirty.clear();
                    		}
                		}
            		}
            		loopCounter = 0;
            	}
            }
            catch(InterruptedException e)
            {
            	alarmDistributor.interrupt();
                saveChangedUsers();
            	if (Logger.getLogger(getClass()).isInfoEnabled())
            	{
            	    Logger.getLogger(getClass()).info("AppointmentManager ready for shutdown");
            	}
                stop = true;
            }
        }
    }
    
    private void expireUnrepeatedEvents() 
    {
    	if (Logger.getLogger(getClass()).isInfoEnabled())
    	{
        	Logger.getLogger(getClass()).info("checking for unrepeated events to expire");
    	}
    	
    	File appDir = new File(appointmentDir);
    	File[] userFiles = appDir.listFiles();
    	if (userFiles == null) {
        	Logger.getLogger(getClass()).error("failed to list appointment user files");
        	return;
    	}
    	
    	long now = System.currentTimeMillis();
    	
    	long appointmentExpirationMillis = WebFileSys.getInstance().getCalendarExpirationPeriod() * 24 * 60 * 60 * 1000;
    	
    	for (int i = 0; i < userFiles.length; i++) 
    	{
    		File userFile = userFiles[i];
    		if (userFile.isFile() && userFile.canRead()) 
    		{
    			String userFileName = userFile.getName();
    			if (userFileName.endsWith(".xml")) 
    			{
    				String userid = userFileName.substring(0, userFileName.lastIndexOf('.'));
    				
    		    	if (Logger.getLogger(getClass()).isDebugEnabled())
    		    	{
    		        	Logger.getLogger(getClass()).debug("checking expiration for user " + userid);
    		    	}

    		    	ArrayList<Appointment> userAppointments = getListOfAppointments(userid, false);
    				
    				if (userAppointments != null)
    				{
    					Iterator<Appointment> iter = userAppointments.iterator();
    					while (iter.hasNext()) {
    						Appointment appointment = iter.next();
    						if (appointment.getRepeatPeriod() == AlarmEntry.REPEAT_NONE) 
    						{
        						if (now - appointment.getEventTime().getTime() > appointmentExpirationMillis)
        						{
        							removeAppointment(userid, appointment.getId());
        					    	if (Logger.getLogger(getClass()).isInfoEnabled())
        					    	{
        				        	    Logger.getLogger(getClass()).info("removing expired appointment: " + appointment.getId() + " for user " + userid);
        					    	}
        						}
    						}
    					}
    				}
    			}
    		}
    	}
    }
    
    public Element getAppointmentList(String userid)
    {
        Element appointmentList=(Element) appointmentMap.get(userid);

        if (appointmentList!=null)
        {
            return(appointmentList);
        }
    
		String appointmentFileName = appointmentDir + File.separator + userid + ".xml";

        File appointmentFile = new File(appointmentFileName);

        if (appointmentFile.exists() && appointmentFile.isFile())
        {
            if (!appointmentFile.canRead())
            {
            	Logger.getLogger(getClass()).error("cannot read appointment file for user " + userid);
                return(null);
            }

            appointmentList=readAppointmentList(appointmentFile.getAbsolutePath());

            if (appointmentList!=null)
            {
                appointmentMap.put(userid,appointmentList);
                createIndex(appointmentList,userid);
                
                return(appointmentList);
            }

        }
        
        return(null);
    }

    Element readAppointmentList(String appointmentFilePath)
    {
        File appointmentFile = new File(appointmentFilePath);

        if ((!appointmentFile.exists()) || (!appointmentFile.canRead()))
        {
        	Logger.getLogger(getClass()).error("appointment file not found or not readable: " + appointmentFilePath);
            return(null);
        }
        
        Document doc = null;

        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream(appointmentFile);
            
            InputSource inputSource = new InputSource(fis);
            
            inputSource.setEncoding("UTF-8");

            if (Logger.getLogger(getClass()).isDebugEnabled())
            {
                Logger.getLogger(getClass()).debug("reading appointments from " + appointmentFilePath);
            }

            doc = builder.parse(inputSource);
        }
        catch (SAXException saxex)
        {
            Logger.getLogger(getClass()).error("failed to load appointment file : " + appointmentFilePath, saxex);
        }
        catch (IOException ioex)
        {
            Logger.getLogger(getClass()).error("failed to load appointment file : " + appointmentFilePath, ioex);
        }
        finally 
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (Exception ex)
                {
                }
            }
        }
        
        return(doc.getDocumentElement());
    }
    
    protected void createIndex(Element appointmentList,String userid)
    {
        NodeList appointments=appointmentList.getElementsByTagName("appointment");

        if (appointments==null)
        {
            indexTable.remove(userid);
            return;
        }

        int listLength=appointments.getLength();

        HashMap<String, Element> userIndex = new HashMap<String, Element>();

        for (int i=0;i<listLength;i++)
        {
             Element appointment=(Element) appointments.item(i);

             String appointmentId=appointment.getAttribute("id");

             if (appointmentId!=null)
             {
                 userIndex.put(appointmentId,appointment);
             }
        }

        indexTable.put(userid, userIndex);
    }

    public void disposeAppointmentList(String userid)
    {
        Boolean dirtyFlag=(Boolean) cacheDirty.get(userid);

        if ((dirtyFlag!=null) && dirtyFlag.booleanValue())
        {
            saveToFile(userid);
        }
        
        appointmentMap.remove(userid);
        indexTable.remove(userid);
    }

    public void disposeAllAppointments()
    {
        saveChangedUsers();

        appointmentMap = new HashMap<String, Element>();
        indexTable = new HashMap<String, HashMap<String, Element>>();
    }

    public ArrayList<String> getAppointmentIds(String userid)
    {
        Element appointmentList=getAppointmentList(userid);

        ArrayList<String> appointmentIds=null;

        if (appointmentList==null)
        {
            return(null);
        }

        NodeList appointments=appointmentList.getElementsByTagName("appointment");

        if (appointments!=null)
        {
            int listLength=appointments.getLength();

            for (int i=0;i<listLength;i++)
            {
                Element appointment=(Element) appointments.item(i);

                if (appointmentIds==null)
                {
                    appointmentIds = new ArrayList<String>();
                }

                appointmentIds.add(appointment.getAttribute("id"));
            }
        }
        else
        {
        	Logger.getLogger(getClass()).info("no appointments found for userid " + userid);
        }
    
        return(appointmentIds);
    }

    public ArrayList<Appointment> getListOfAppointments(String userid)
    {
        return(getListOfAppointments(userid,true));
    }
    
    public ArrayList<Appointment> getListOfAppointments(String userid, boolean createIfMissing)
    {
        Element appointmentList=getAppointmentList(userid);

        ArrayList<Appointment> listOfAppointments = null;

        if (appointmentList==null)
        {
            if (createIfMissing)
            {
                // todo: create a new notes list for this user
                return(null);
            }
        }

        NodeList appointments=appointmentList.getElementsByTagName("appointment");

        if (appointments!=null)
        {
            AppointmentComparator appointmentComparator=new AppointmentComparator();

            int listLength=appointments.getLength();

            for (int i=0;i<listLength;i++)
            {
                Element appointment=(Element) appointments.item(i);

                if (listOfAppointments==null)
                {
                    listOfAppointments = new ArrayList<Appointment>();
                }

                Appointment newAppointment=new Appointment(appointment.getAttribute("id"));

                parseAppointment(appointment,newAppointment);

                boolean stop=false;
                for (int k=0;(!stop) && (k<listOfAppointments.size());k++)
                {
                    if (appointmentComparator.compare(newAppointment,listOfAppointments.get(k))<0)
                    {
                        listOfAppointments.add(k, newAppointment);
                        stop=true;
                    }
                }
                
                if (!stop)
                {
                    listOfAppointments.add(newAppointment);
                }
            }
        }
    
        return(listOfAppointments);
    }

    public void parseAppointment(Element appointment,Appointment newAppointment)
    {
        newAppointment.setSubject(XmlUtil.getChildText(appointment,"subject"));
        newAppointment.setContent(XmlUtil.getChildText(appointment,"content"));
        newAppointment.setCopyReceiver(XmlUtil.getChildText(appointment,"copyReceiver"));

        String scheduleIdString=XmlUtil.getChildText(appointment,"scheduleId");

        long scheduleId=0L;
        try
        {
            scheduleId=Long.parseLong(scheduleIdString);
        }
        catch (NumberFormatException nfe)
        {
        }
        newAppointment.setScheduleId(scheduleId);

        int alarmType=AlarmEntry.ALARM_NONE;
        String alarmTypeString=XmlUtil.getChildText(appointment,"alarmType");
        try
        {
            alarmType=Integer.parseInt(alarmTypeString);
            newAppointment.setAlarmType(alarmType);
        }
        catch (NumberFormatException nfe)
        {
        	Logger.getLogger(getClass()).error("failed to read appointment: ", nfe);
        }

        int repeatPeriod=AlarmEntry.REPEAT_NONE;
        String repeatPeriodString=XmlUtil.getChildText(appointment,"repeatPeriod");
        try
        {
            repeatPeriod=Integer.parseInt(repeatPeriodString);
            newAppointment.setRepeatPeriod(repeatPeriod);
        }
        catch (NumberFormatException nfe)
        {
        	Logger.getLogger(getClass()).error("failed to read appointment", nfe);
        }

        String fullDay = XmlUtil.getChildText(appointment,"fullDay");
        if ((fullDay != null) && (fullDay.equalsIgnoreCase("true"))) 
        {
        	newAppointment.setFullDay(true);
        	
        	String fullDayNumStr = XmlUtil.getChildText(appointment,"fullDayNum");
        	if (fullDayNumStr != null) 
        	{
        		newAppointment.setFullDayNum(Integer.parseInt(fullDayNumStr));
        	}
        }
        
        String alarmed=XmlUtil.getChildText(appointment,"alarmed");
        newAppointment.setAlarmed((alarmed!=null) && alarmed.equalsIgnoreCase("true"));

        String mailAlarmed=XmlUtil.getChildText(appointment,"mailAlarmed");
        newAppointment.setMailAlarmed((mailAlarmed!=null) && mailAlarmed.equalsIgnoreCase("true"));

        String repeatChecked=XmlUtil.getChildText(appointment,"repeatChecked");
        newAppointment.setRepeatChecked((repeatChecked!=null) && repeatChecked.equalsIgnoreCase("true"));

        long eventTime=0L;
        long alarmTime=0L;
        long duration=0L;
        try
        {
            String timeString=XmlUtil.getChildText(appointment,"eventTime");
            eventTime=Long.parseLong(timeString);

            timeString=XmlUtil.getChildText(appointment,"alarmTime");
            alarmTime=Long.parseLong(timeString);

            timeString=XmlUtil.getChildText(appointment,"duration");
            duration=Long.parseLong(timeString);

            timeString = XmlUtil.getChildText(appointment, "lastMailAlarmed");
            if (!CommonUtils.isEmpty(timeString))
            {
                newAppointment.setLastMailAlarmed(new Date(Long.parseLong(timeString)));
            }
        }
        catch (NumberFormatException nfe)
        {
        	Logger.getLogger(getClass()).error("failed to read appointment", nfe);
        }

        newAppointment.setEventTime(new Date(eventTime));
        newAppointment.setAlarmTime(new Date(alarmTime));
        newAppointment.setDuration(duration);

        long creationTime=0L;
        String timeString=XmlUtil.getChildText(appointment,"creationTime");
        try
        {
            creationTime=Long.parseLong(timeString);
        }
        catch (NumberFormatException nfe)
        {
        	Logger.getLogger(getClass()).error("invalid appointment creation time", nfe);
            creationTime=(new Date()).getTime();
        }

        newAppointment.setCreationTime(new Date(creationTime));

        long updateTime=0L;
        timeString=XmlUtil.getChildText(appointment,"updateTime");
        try
        {
            updateTime=Long.parseLong(timeString);
        }
        catch (NumberFormatException nfe)
        {
        	Logger.getLogger(getClass()).error("invalid appointment update time", nfe);
            updateTime=(new Date()).getTime();
        }

        newAppointment.setUpdateTime(new Date(updateTime));
    }

    public Appointment getAppointment(String userid,String searchedId)
    {
        Element appointment=getAppointmentElement(userid,searchedId);

        if (appointment==null)
        {
        	Logger.getLogger(getClass()).error("appointment for user " + userid + " id " + searchedId + " not found");
            return(null);
        }
        
        Appointment foundAppointment=new Appointment(appointment.getAttribute("id"));

        parseAppointment(appointment,foundAppointment);

        return(foundAppointment);
    }

    protected Element getAppointmentElement(String userid,String searchedId)
    {
        Element appointmentList=getAppointmentList(userid);

        if (appointmentList==null)
        {
            return(null);
        }
        
        Element appointment=null;

        HashMap<String, Element> userIndex = (HashMap<String, Element>) indexTable.get(userid);

        if (userIndex!=null)
        {
            appointment=(Element) userIndex.get(searchedId);

            if (appointment!=null)
            {
                return(appointment);
            }
        }

    	Logger.getLogger(getClass()).error("appointment with id " + searchedId + " not found in index");

        NodeList appointments=appointmentList.getElementsByTagName("appointment");

        if (appointments==null)
        {
            return(null);
        }

        int listLength=appointments.getLength();

        for (int i=0;i<listLength;i++)
        {
            appointment=(Element) appointments.item(i);

            if (appointment.getAttribute("id").equals(searchedId))
            {
                return(appointment);
            }
        }
    
        return(null);
    }

    protected Element createAppointmentList(String userid)
    {
    	Logger.getLogger(getClass()).info("creating new appointment list for user : " + userid);
        
        Document doc=builder.newDocument();

        Element appointmentListElement=doc.createElement("appointmentList");

        Element lastIdElement=doc.createElement("lastId");
        XmlUtil.setElementText(lastIdElement,"0");

        appointmentListElement.appendChild(lastIdElement);
        
        doc.appendChild(appointmentListElement);

        appointmentMap.put(userid,appointmentListElement);

        indexTable.put(userid, new HashMap<String, Element>());

        return(appointmentListElement);
    }

    /**
     * @return the appointment id of the new created appointment
     */
    public String createAppointment(String userid, Appointment newAppointment)
    {
    	Element appointmentList = getAppointmentList(userid);

        if (appointmentList==null)
        {
            appointmentList=createAppointmentList(userid);
        }

        String newIdString=null;

        synchronized (appointmentList)
        {
            Document doc=appointmentList.getOwnerDocument();

            Element newElement=doc.createElement("appointment");

            newElement.appendChild(doc.createElement("subject"));
            newElement.appendChild(doc.createElement("content"));
            newElement.appendChild(doc.createElement("copyReceiver"));
            newElement.appendChild(doc.createElement("eventTime"));
            newElement.appendChild(doc.createElement("alarmTime"));
            newElement.appendChild(doc.createElement("duration"));
            newElement.appendChild(doc.createElement("alarmed"));
            newElement.appendChild(doc.createElement("mailAlarmed"));
            newElement.appendChild(doc.createElement("repeatChecked"));
            newElement.appendChild(doc.createElement("alarmType"));
            newElement.appendChild(doc.createElement("repeatPeriod"));

            newElement.appendChild(doc.createElement("scheduleId"));
            newElement.appendChild(doc.createElement("creationTime"));
            newElement.appendChild(doc.createElement("updateTime"));

            appointmentList.appendChild(newElement);

            int lastId=getLastId(userid);

            lastId++;

            setLastId(userid,lastId);

            newIdString="" + lastId;

            newAppointment.setId(newIdString);
            newElement.setAttribute("id",newIdString);

            HashMap<String, Element> userIndex = (HashMap<String, Element>) indexTable.get(userid);
            userIndex.put(newIdString,newElement);
        }

        updateAppointment(userid,newAppointment, true);

        return(newIdString);
    }

    protected int getLastId(String userid)
    {
        Element appointmentList=getAppointmentList(userid);

        if (appointmentList==null)
        {
            return(-1);
        }

        String lastIdString=XmlUtil.getChildText(appointmentList,"lastId").trim();

        int lastId=0;
        try
        {
            lastId=Integer.parseInt(lastIdString);
        }
        catch (NumberFormatException nfe)
        {
        	Logger.getLogger(getClass()).error("failed to get last id", nfe);
        }

        return(lastId);
    }

    protected void setLastId(String userid,int lastId)
    {
        Element appointmentList=getAppointmentList(userid);

        if (appointmentList==null)
        {
            return;
        }

        XmlUtil.setChildText(appointmentList,"lastId","" + lastId);
    }

    public Element updateAppointment(String userid, Appointment changedAppointment, boolean updateAlarmIdx)
    {
        Element appointmentElement = null;

        Element appointmentListElement = getAppointmentList(userid);

        synchronized (appointmentListElement)
        {
            appointmentElement = getAppointmentElement(userid,changedAppointment.getId());

            if (appointmentElement == null)
            {
            	Logger.getLogger(getClass()).error("updateAppointment: appointment for user " + userid + " with id " + changedAppointment.getId() +  " not found");            	
                return(null);
            }

            XmlUtil.setChildText(appointmentElement,"subject",changedAppointment.getSubject(),true);
            XmlUtil.setChildText(appointmentElement,"content",changedAppointment.getContent(),true);
            if (changedAppointment.getCopyReceiver()!=null)
            {
            	XmlUtil.setChildText(appointmentElement,"copyReceiver",changedAppointment.getCopyReceiver());
            }
            XmlUtil.setChildText(appointmentElement,"eventTime","" + changedAppointment.getEventTime().getTime());
            XmlUtil.setChildText(appointmentElement,"alarmTime","" + changedAppointment.getAlarmTime().getTime());
            XmlUtil.setChildText(appointmentElement,"duration","" + changedAppointment.getDuration());
            XmlUtil.setChildText(appointmentElement,"alarmed", Boolean.toString(changedAppointment.isAlarmed()));
            XmlUtil.setChildText(appointmentElement,"mailAlarmed", Boolean.toString(changedAppointment.isMailAlarmed()));
            if (changedAppointment.getLastMailAlarmed() != null)
            {
                XmlUtil.setChildText(appointmentElement,"lastMailAlarmed", Long.toString(changedAppointment.getLastMailAlarmed().getTime()));
            }
            XmlUtil.setChildText(appointmentElement,"repeatChecked", Boolean.toString(changedAppointment.isRepeatChecked()));
            XmlUtil.setChildText(appointmentElement,"alarmType","" + changedAppointment.getAlarmType());
            XmlUtil.setChildText(appointmentElement,"repeatPeriod","" + changedAppointment.getRepeatPeriod());
            XmlUtil.setChildText(appointmentElement,"scheduleId","" + changedAppointment.getScheduleId());
            XmlUtil.setChildText(appointmentElement,"creationTime","" + changedAppointment.getCreationTime().getTime());
            XmlUtil.setChildText(appointmentElement,"updateTime","" + new Date().getTime());

            XmlUtil.setChildText(appointmentElement,"fullDay", Boolean.toString(changedAppointment.isFullday()));
            XmlUtil.setChildText(appointmentElement,"fullDayNum", Integer.toString(changedAppointment.getFullDayNum()));
            
            // saveToFile(userid);
            cacheDirty.put(userid, new Boolean(true));
        }
        
        if (updateAlarmIdx)
        {
    		alarmIdx.delEvent(userid, changedAppointment);
    		
    		alarmIdx.addEvent(userid, changedAppointment);
        }
        
        return(appointmentElement);
    }

    public void removeAppointment(String userid, String searchedId)
    {
    	Appointment appToRemove = getAppointment(userid, searchedId);

    	if (appToRemove != null)
    	{
            alarmIdx.delEvent(userid, appToRemove);
    	}
    	
        Element appointmentListElement=getAppointmentList(userid);

        synchronized (appointmentListElement)
        {
            Element appointmentElement=getAppointmentElement(userid,searchedId);

            if (appointmentElement==null)
            {
            	Logger.getLogger(getClass()).error("appointment for user " + userid + " id " + searchedId + " not found");
                return;
            }

            Node appointmentList=appointmentElement.getParentNode();

            if (appointmentList!=null)
            {
                HashMap<String, Element> userIndex=(HashMap<String, Element>) indexTable.get(userid);
                userIndex.remove(appointmentElement.getAttribute("id"));

                appointmentList.removeChild(appointmentElement);

                // saveToFile(userid);
                cacheDirty.put(userid,new Boolean(true));
            }
        }
    }

    /**
     * Appointments for Alarms.
     * @param forDay
     * @param userid
     * @return
     */
	public ArrayList<Appointment> getAppointmentsForDay(Calendar forDayCal, String userid)
    {
		forDayCal.set(Calendar.HOUR_OF_DAY, 0);
		
		forDayCal.set(Calendar.MINUTE, 0);

		forDayCal.set(Calendar.SECOND, 0);
		
		forDayCal.set(Calendar.MILLISECOND, 0);

		/*
		long timeZoneOffset = forDayCal.getTimeZone().getOffset(forDayCal.getTimeInMillis());
		long startTime = forDayCal.getTimeInMillis() - timeZoneOffset;
		*/
		
		long startTime = forDayCal.getTimeInMillis();

		long endTime = startTime + (1000l * 60l * 60l * 24l);

        return(getAppointmentsForDateRange(userid, startTime, endTime));
    }

    /**
     * Appointments for display.
     * @param forDay
     * @param userid
     * @return
     */
	public ArrayList<Appointment> getDayAppointments(Date forDay, String userid)
	{
		// TODO: timezone in user object
		// int userTimezone = XmlUserProfileManager.getInstance().getTimeZone(userid);
        int userTimezone = 1;

		Calendar cal = Calendar.getInstance();
        
		cal.setTime(forDay);
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		
		cal.set(Calendar.MINUTE, 0);

		cal.set(Calendar.SECOND, 0);

		long zeroGMT = cal.getTime().getTime();
		
		long startTime = zeroGMT - (userTimezone * 3600000l);

		long endTime = startTime + (1000l * 60l * 60l * 24l);

		return(getAppointmentsForDateRange(userid, startTime, endTime));
	}

    public ArrayList<Appointment> getAppointmentsForDateRange(String userid, long startTime, long endTime)
    {
    	Element appointmentList = getAppointmentList(userid);
    	
		ArrayList<Appointment> listOfAppointments = new ArrayList<Appointment>();

		if (appointmentList == null)
		{
            return(listOfAppointments);
		}

		NodeList appointments = appointmentList.getElementsByTagName("appointment");

		if (appointments != null)
		{
			int listLength = appointments.getLength();

			for (int i = 0; i < listLength; i++)
			{
				Element appointment = (Element) appointments.item(i);

                long eventTime = 0l;

                String eventTimeVal = XmlUtil.getChildText(appointment, "eventTime");
                
                if (eventTimeVal != null)
                {
					try
					{
						eventTime = Long.parseLong(eventTimeVal);
					}
					catch (NumberFormatException nfex)
					{
						Logger.getLogger(getClass()).error("invalid event time in appointment of user " + userid);
					}
                }

                if ((eventTime >= startTime) && (eventTime <= endTime))
                {
					Appointment newAppointment = new Appointment(appointment.getAttribute("id"));

					parseAppointment(appointment, newAppointment);

					listOfAppointments.add(newAppointment);
				}
			}
		}

		Collections.sort(listOfAppointments, new AppointmentComparator());
    
		return(listOfAppointments);
    }

    public void addRepeatedAppointmentClones(String userid, long startTime, long endTime, 
    	ArrayList<Appointment> listOfAppointments)
    {
    	Element appointmentList = getAppointmentList(userid);
    	
		if (appointmentList == null)
		{
            return;
		}

		NodeList appointments = appointmentList.getElementsByTagName("appointment");

		if (appointments != null)
		{
			ArrayList<Appointment> dailyAppointments = new ArrayList<Appointment>();

			ArrayList<Appointment> weeklyAppointments = new ArrayList<Appointment>();

			ArrayList<Appointment> monthlyAppointments = new ArrayList<Appointment>();

			ArrayList<Appointment> annualAppointments = new ArrayList<Appointment>();
			
			ArrayList<Appointment> weekdayAppointments = new ArrayList<Appointment>();

			ArrayList<Appointment> multiDayAppointments = new ArrayList<Appointment>();

    		Calendar startTimeCal = new GregorianCalendar();
    		startTimeCal.setTimeInMillis(startTime);
			
			int listLength = appointments.getLength();

			for (int i = 0; i < listLength; i++)
			{
				Element appointmentElem = (Element) appointments.item(i);
				
				String repeatPeriod = XmlUtil.getChildText(appointmentElem, "repeatPeriod");
				
				if (repeatPeriod.equals(Integer.toString(AlarmEntry.REPEAT_DAILY)) ||
					repeatPeriod.equals(Integer.toString(AlarmEntry.REPEAT_WEEKLY)) ||
					repeatPeriod.equals(Integer.toString(AlarmEntry.REPEAT_WEEKDAY)) ||
					repeatPeriod.equals(Integer.toString(AlarmEntry.REPEAT_MONTHLY)) ||
					repeatPeriod.equals(Integer.toString(AlarmEntry.REPEAT_ANNUAL)))
				{
	                String eventTimeVal = XmlUtil.getChildText(appointmentElem, "eventTime");
	                
	                if (eventTimeVal != null)
	                {
		                long eventTime = 0l;
		                
						try
						{
							eventTime = Long.parseLong(eventTimeVal);

							if (eventTime <= startTime)
			                {
								Appointment appointment = new Appointment(appointmentElem.getAttribute("id"));
								parseAppointment(appointmentElem, appointment);

								if (repeatPeriod.equals(Integer.toString(AlarmEntry.REPEAT_DAILY)))
								{
									dailyAppointments.add(appointment);
								}
								else if (repeatPeriod.equals(Integer.toString(AlarmEntry.REPEAT_WEEKLY)))
								{
									weeklyAppointments.add(appointment);
								}
								else if (repeatPeriod.equals(Integer.toString(AlarmEntry.REPEAT_MONTHLY)))
								{
									monthlyAppointments.add(appointment);
								}
								else if (repeatPeriod.equals(Integer.toString(AlarmEntry.REPEAT_ANNUAL)))
								{
									annualAppointments.add(appointment);
								}
								else if (repeatPeriod.equals(Integer.toString(AlarmEntry.REPEAT_WEEKDAY)))
								{
									weekdayAppointments.add(appointment);
								}
			                }
						}
						catch (NumberFormatException nfex)
						{
							Logger.getLogger(getClass()).error("invalid event time in appointment of user " + userid + " : " + eventTimeVal);
						}
	                }
				}
				else
				{
					if (startTimeCal.get(Calendar.DAY_OF_MONTH) == 1) 
					{
						// check for cross month border multi-day events only for the first day in month
						try 
						{
						    String fullDayNumStr = XmlUtil.getChildText(appointmentElem, "fullDayNum");
						    if (!CommonUtils.isEmpty(fullDayNumStr)) 
						    {
						    	int fullDayNum = Integer.parseInt(fullDayNumStr);
						    	if (fullDayNum > 1) 
						    	{
									Appointment appointment = new Appointment(appointmentElem.getAttribute("id"));
									parseAppointment(appointmentElem, appointment);
									
									if (appointment.getEventTime().getTime() <= startTime)
									{
										multiDayAppointments.add(appointment);
									}
						    	}
						    }
						}
						catch (NumberFormatException numEx) 
						{
							Logger.getLogger(getClass()).error("invalid fullDayNum value in appointment of user " + userid, numEx);
						}
					}
				}
			}
			
			// create a clone of daily appointments for each day of the date range  
			
			Iterator<Appointment> iter = dailyAppointments.iterator();
			
			Calendar cloneCal = new GregorianCalendar();
			
			while (iter.hasNext()) 
			{
				Appointment dailyApp = iter.next();

				long alarmOffset = dailyApp.getEventTime().getTime() - dailyApp.getAlarmTime().getTime();
				
				cloneCal.setTime(dailyApp.getEventTime());
				
				cloneCal.add(Calendar.DAY_OF_MONTH, 1);
				
				while (cloneCal.getTimeInMillis() < endTime)
				{
					if (cloneCal.getTimeInMillis() >= startTime)
					{
						Appointment dailyClone = dailyApp.getClone();
						dailyClone.setId(dailyApp.getId());
						
						dailyClone.setEventTime(new Date(cloneCal.getTimeInMillis()));
						dailyClone.setAlarmTime(new Date(cloneCal.getTimeInMillis() - alarmOffset));

						listOfAppointments.add(dailyClone);
					}

					cloneCal.add(Calendar.DAY_OF_MONTH, 1);
				}
			}

			// create a clone of weekly appointments for each week of the date range  
			
			iter = weeklyAppointments.iterator();
			
			while (iter.hasNext()) 
			{
				Appointment weeklyApp = iter.next();

				long alarmOffset = weeklyApp.getEventTime().getTime() - weeklyApp.getAlarmTime().getTime();
				
				cloneCal.setTime(weeklyApp.getEventTime());
				
				cloneCal.add(Calendar.DAY_OF_MONTH, 7);
				
				while (cloneCal.getTimeInMillis() < endTime)
				{
					if (cloneCal.getTimeInMillis() >= startTime)
					{
						Appointment weeklyClone = weeklyApp.getClone();
						weeklyClone.setId(weeklyApp.getId());
						
						weeklyClone.setEventTime(new Date(cloneCal.getTimeInMillis()));
						weeklyClone.setAlarmTime(new Date(cloneCal.getTimeInMillis() - alarmOffset));

						listOfAppointments.add(weeklyClone);
					}

					cloneCal.add(Calendar.DAY_OF_MONTH, 7);
				}
			}
			
			// create a clone of monthly appointments for the month of the date range  
			
			iter = monthlyAppointments.iterator();
			
			while (iter.hasNext()) 
			{
				Appointment monthlyApp = iter.next();

				long alarmOffset = monthlyApp.getEventTime().getTime() - monthlyApp.getAlarmTime().getTime();
				
				cloneCal.setTime(monthlyApp.getEventTime());
				
				cloneCal.add(Calendar.MONTH, 1);
				
				while (cloneCal.getTimeInMillis() < endTime)
				{
					if (cloneCal.getTimeInMillis() >= startTime)
					{
						Appointment monthlyClone = monthlyApp.getClone();
						monthlyClone.setId(monthlyApp.getId());
						
						monthlyClone.setEventTime(new Date(cloneCal.getTimeInMillis()));
						monthlyClone.setAlarmTime(new Date(cloneCal.getTimeInMillis() - alarmOffset));

						listOfAppointments.add(monthlyClone);
					}

					cloneCal.add(Calendar.MONTH, 1);
				}
			}

			// create a clone of annual appointments for the month of the date range  
			
			iter = annualAppointments.iterator();
			
			while (iter.hasNext()) 
			{
				Appointment annualApp = iter.next();

				long alarmOffset = annualApp.getEventTime().getTime() - annualApp.getAlarmTime().getTime();
				
				cloneCal.setTime(annualApp.getEventTime());
				
				cloneCal.add(Calendar.YEAR, 1);
				
				while (cloneCal.getTimeInMillis() < endTime)
				{
					if (cloneCal.getTimeInMillis() >= startTime)
					{
						Appointment annualClone = annualApp.getClone();
						annualClone.setId(annualApp.getId());
						
						annualClone.setEventTime(new Date(cloneCal.getTimeInMillis()));
						annualClone.setAlarmTime(new Date(cloneCal.getTimeInMillis() - alarmOffset));

						listOfAppointments.add(annualClone);
					}

					cloneCal.add(Calendar.YEAR, 1);
				}
			}
			
			// create a clone of weekday appointments for each weekday of the date range  
			
			iter = weekdayAppointments.iterator();
			
			while (iter.hasNext()) 
			{
				Appointment weekdayApp = iter.next();

				long alarmOffset = weekdayApp.getEventTime().getTime() - weekdayApp.getAlarmTime().getTime();
				
				cloneCal.setTime(weekdayApp.getEventTime());
				
				if (cloneCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
				{
					cloneCal.add(Calendar.DAY_OF_MONTH, 3);
				}
				else if (cloneCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
				{
					cloneCal.add(Calendar.DAY_OF_MONTH, 2);
				}
				else
				{
					cloneCal.add(Calendar.DAY_OF_MONTH, 1);
				}
				
				while (cloneCal.getTimeInMillis() < endTime)
				{
					if (cloneCal.getTimeInMillis() >= startTime)
					{
						Appointment weekdayClone = weekdayApp.getClone();
						weekdayClone.setId(weekdayApp.getId());
						
						weekdayClone.setEventTime(new Date(cloneCal.getTimeInMillis()));
						weekdayClone.setAlarmTime(new Date(cloneCal.getTimeInMillis() - alarmOffset));

						listOfAppointments.add(weekdayClone);
					}

					if (cloneCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
					{
						cloneCal.add(Calendar.DAY_OF_MONTH, 3);
					}
					else if (cloneCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
					{
						cloneCal.add(Calendar.DAY_OF_MONTH, 2);
					}
					else
					{
						cloneCal.add(Calendar.DAY_OF_MONTH, 1);
					}
				}
			}
			
			// create a clone of multi-day appointments that cross month borders 
			
			iter = multiDayAppointments.iterator();
			
			while (iter.hasNext()) 
			{
				Appointment multiDayApp = iter.next();

				long eventEndTime = multiDayApp.getEventTime().getTime() + (((long) multiDayApp.getFullDayNum()) * 1000l * 3600l * 24l);
				
				// ignore events that are already history (end time is previous month or earlier)
				if (eventEndTime > startTime) {
					cloneCal.setTimeInMillis(startTime);

					Calendar eventStartCal = new GregorianCalendar();
					eventStartCal.setTime(multiDayApp.getEventTime());

					if (cloneCal.get(Calendar.MONTH) != eventStartCal.get(Calendar.MONTH))
					{
						// cross month border
						
						int daysInPrevMonth = 0;
						while ((eventStartCal.get(Calendar.DAY_OF_MONTH) != 1) ||
							   (eventStartCal.get(Calendar.MONTH) != cloneCal.get(Calendar.MONTH)))
						{
							eventStartCal.add(Calendar.DAY_OF_MONTH, 1);
							daysInPrevMonth++;
						}
						
						Appointment multiMonthClone = multiDayApp.getClone();
						multiMonthClone.setId(multiDayApp.getId());
						
						multiMonthClone.setEventTime(cloneCal.getTime());
						
						multiMonthClone.setFullDayNum(multiMonthClone.getFullDayNum() - daysInPrevMonth);

						listOfAppointments.add(multiMonthClone);
					}
				}
			}
		}
    }
    
	public ArrayList<Appointment> getAlarmsForDay(Date forDay, String userid)
	{
		// TODO: timezone in user object
		// int userTimezone = XmlUserProfileManager.getInstance().getTimeZone(userid);
        int userTimezone = 1;

		Calendar cal = Calendar.getInstance();
        
		cal.setTime(forDay);
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		
		cal.set(Calendar.MINUTE, 0);

		cal.set(Calendar.SECOND, 0);

		long zeroGMT = cal.getTime().getTime();
		
		long startTime = zeroGMT + (userTimezone * 3600000l);

		long endTime = startTime + (1000l * 60l * 60l * 24l);

		return(getAlarmsForDateRange(userid, startTime, endTime));
	}

	public ArrayList<Appointment> getAlarmsForDateRange(String userid, long startTime, long endTime)
	{
		Element appointmentList = this.getAppointmentList(userid);
    	
		ArrayList<Appointment> listOfAppointments = new ArrayList<Appointment>();

		if (appointmentList == null)
		{
			Logger.getLogger(getClass()).error("appointment list for user " + userid + " does not exist!");

			return(listOfAppointments);
		}

		NodeList appointments = appointmentList.getElementsByTagName("appointment");

		if (appointments!=null)
		{
			int listLength = appointments.getLength();

			for (int i = 0; i < listLength; i++)
			{
				Element appointment = (Element) appointments.item(i);

				long alarmTime = 0l;

				String alarmTimeVal = XmlUtil.getChildText(appointment, "alarmTime");
                
				if (alarmTimeVal != null)
				{
					try
					{
						alarmTime = Long.parseLong(alarmTimeVal);
					}
					catch (NumberFormatException nfex)
					{
						Logger.getLogger(getClass()).error("invalid alarm time in appointment of user " + userid);
					}
				}

				if ((alarmTime >= startTime) && (alarmTime <= endTime))
				{
					Appointment newAppointment = new Appointment(appointment.getAttribute("id"));

					parseAppointment(appointment, newAppointment);

					listOfAppointments.add(newAppointment);
				}
			}
		}
		
		Collections.sort(listOfAppointments, new AppointmentComparator());
    
		return(listOfAppointments);
	}

    protected synchronized void saveToFile(String userid)
    {
        Element appointmentListElement=getAppointmentList(userid);

        if (appointmentListElement==null)
        {
        	Logger.getLogger(getClass()).error("appointment list for user " + userid + " does not exist");
            return;
        }

        Logger.getLogger(getClass()).debug("saving appointments for user " + userid);

        File appointmentDirFile = new File(appointmentDir);
        if (!appointmentDirFile.exists()) 
        {
        	if (appointmentDirFile.mkdirs())
        	{
        		Logger.getLogger(getClass()).debug("appointment folder created");
        	}
        	else
        	{
        		Logger.getLogger(getClass()).error("appointment folder could not be created");
        		return;
        	}
        }
        
        synchronized (appointmentListElement)
        {
			String xmlFileName = appointmentDir + File.separator + userid + ".xml";

            OutputStreamWriter xmlOutFile = null;

            try
            {
                FileOutputStream fos = new FileOutputStream(xmlFileName);
                
                xmlOutFile = new OutputStreamWriter(fos, "UTF-8");
                
                XmlUtil.writeToStream(appointmentListElement, xmlOutFile);

                xmlOutFile.flush();
            }
            catch (IOException io1)
            {
            	Logger.getLogger(getClass()).error("failed to save appointments for user " + userid, io1);
            }
            finally
            {
                if (xmlOutFile != null)
                {
                    try 
                    {
                        xmlOutFile.close();
                    }
                    catch (Exception ex) 
                    {
                    }
                }
            }
        }

        // WebReminder.shutdownLocked--;
    }

    public synchronized void saveChangedUsers()
    {
        Iterator<String> cacheUserIter = cacheDirty.keySet().iterator();

        while (cacheUserIter.hasNext())
        {
            String userid = (String) cacheUserIter.next();

            boolean dirtyFlag = cacheDirty.get(userid).booleanValue();

            if (dirtyFlag)
            {
                saveToFile(userid);
                cacheDirty.put(userid,new Boolean(false));
            }
        }
    }
    
    protected void loadAlarmIndexFromXml()
    {
    	Logger.getLogger(getClass()).debug("loading appointments into alarm index");
    	
        alarmIdx = new AlarmIndex();

        File appointmentDirFile = new File(appointmentDir);
        
        String userList[] = appointmentDirFile.list();

        if (userList != null)
        {
        	Calendar todayCal = new GregorianCalendar();
        	
        	todayCal.set(Calendar.HOUR_OF_DAY, 0);
        	todayCal.set(Calendar.MINUTE, 0);
        	todayCal.set(Calendar.SECOND, 0);
        	
            for (int i = 0; i < userList.length; i++)
            {
                String userXmlFileName = userList[i];

                if (userXmlFileName.endsWith(".xml"))
                {
                    String userName = userXmlFileName.substring(0, userXmlFileName.indexOf(".xml"));

                    loadUserAppointmentsFromXml(userName, todayCal);
                }
            }
        }
    }

    private String getTimeZoneOffset(long forTime) 
    {
		StringBuffer tzone = new StringBuffer();
		tzone.append("(GMT");

		TimeZone timeZone = TimeZone.getDefault();
		
		int offset = timeZone.getOffset(forTime);
		
		if (offset >= 0)
		{
			tzone.append("+");
		}
		tzone.append(Integer.toString(offset / (1000 * 60 * 60)));
		tzone.append(")");
		
		return tzone.toString();
    }
    
    protected void loadUserAppointmentsFromXml(String userName, Calendar todayCal)
    {
        boolean downtimeAlarmSent = false;
    	
    	ArrayList<Appointment> appointmentList = getListOfAppointments(userName, false);

        if ((appointmentList != null) && (appointmentList.size() > 0))
        {
            for (int i = 0; i < appointmentList.size(); i++)
            {
                Appointment appointment = appointmentList.get(i);

                AlarmEntry newEvent = alarmIdx.addEvent(userName, appointment);

                appointment.setScheduleId(newEvent.getDateId());

                updateAppointment(userName, appointment, false);

                if (appointment.isAlarmed())
                {
                	if (appointment.getRepeatPeriod() == AlarmEntry.REPEAT_NONE) {
                        newEvent.setAlarmed();
                	}
                	else
                	{
                        newEvent.unsetAlarmed();
                	}
                }

                if (appointment.isMailAlarmed())
                {
                    newEvent.setMailAlarmed();
                }

            	if ((appointment.getAlarmType() == AlarmEntry.ALARM_MAIL) ||
                	(appointment.getAlarmType() == AlarmEntry.ALARM_ALL))
            	{
            		if (appointment.getAlarmTime().getTime() < todayCal.getTimeInMillis()) 
            		{
            			// alarm(s) missed during server downtime - send it now 
                    	
            			if (appointment.getRepeatPeriod() == AlarmEntry.REPEAT_NONE)
            			{
            				if (!appointment.isMailAlarmed())
            				{
            					if (Logger.getLogger(getClass()).isDebugEnabled()) 
            					{
                        			Logger.getLogger(getClass()).debug("sending downtime alarm for user " + userName);
            					}

            					// TODO: take user's timezone from user profile and calculate time
            					// and timezone offset to show in the mail
            					// int tzHourOffset = XmlUserProfileManager.getInstance().getTimeZone(userName);
            					
            					String tzone = getTimeZoneOffset(appointment.getAlarmTime().getTime());
            					
                            	if (alarmDistributor.sendAlarmMail(userName, appointment, tzone.toString()))
        						{
        							newEvent.setMailAlarmed();
        							appointment.setMailAlarmed(true);
        							
        							updateAppointment(userName, appointment, false);
        							
        				            downtimeAlarmSent = true;
        						}
            				}
            			}
            			else
            			{
            				if (checkMissedRepeatedAlarms(userName, appointment, newEvent, todayCal))
            				{
    				            downtimeAlarmSent = true;
            				}
            			}
            		}
            	}
            }
        }
        
		if (Logger.getLogger(getClass()).isDebugEnabled())
		{
	    	Logger.getLogger(getClass()).debug("loaded appointments of user " + userName + " into alarm index");
		}
    	
    	if (downtimeAlarmSent)
    	{
    		saveToFile(userName);    	
    	}
    	
    	appointmentMap.remove(userName);    
    	indexTable.remove(userName);
    	cacheDirty.remove(userName);
    }
    
	private boolean checkMissedRepeatedAlarms(String userid, Appointment appointment,
		AlarmEntry newEvent, Calendar todayCal)
	{
		boolean downtimeAlarmSent = false;
		
		Date oldEventTime = appointment.getEventTime();
		
        long alarmOffset = newEvent.getEventDate().getTime() - newEvent.getAlarmTime().getTime();
		
		Date lastMailAlarmed = appointment.getLastMailAlarmed();
		if (lastMailAlarmed == null)
		{
			if (appointment.getAlarmTime().getTime() < todayCal.getTimeInMillis())
			{
				// TODO: take user's timezone from user profile and calculate time
				// and timezone offset to show in the mail
				// int tzHourOffset = XmlUserProfileManager.getInstance().getTimeZone(userid);
				
				String tzone = getTimeZoneOffset(appointment.getAlarmTime().getTime());
				
    			if (alarmDistributor.sendAlarmMail(userid, appointment, tzone, appointment.getAlarmTime()))
				{
					newEvent.setMailAlarmed();
					appointment.setMailAlarmed(true);
		            downtimeAlarmSent = true;
				}
				lastMailAlarmed = appointment.getAlarmTime();
				appointment.setLastMailAlarmed(appointment.getAlarmTime());
				newEvent.setLastMailAlarmed(appointment.getAlarmTime());
			}
			else
			{
				lastMailAlarmed = todayCal.getTime();
				appointment.setLastMailAlarmed(todayCal.getTime());
				newEvent.setLastMailAlarmed(todayCal.getTime());
			}
			
			updateAppointment(userid, appointment, false);
		}
 
	    Calendar alarmCal = new GregorianCalendar();
	    alarmCal.setTime(lastMailAlarmed);

	    alarmCal.set(Calendar.HOUR_OF_DAY, appointment.getEventTime().getHours());
	    alarmCal.set(Calendar.MINUTE, appointment.getEventTime().getMinutes());
	    
	    if (appointment.getRepeatPeriod() == AlarmEntry.REPEAT_DAILY)
	    {
	    	alarmCal.add(Calendar.DAY_OF_MONTH, 1);
	    	while (alarmCal.getTimeInMillis() < todayCal.getTimeInMillis())
	    	{
	    		if (Logger.getLogger(getClass()).isDebugEnabled())
	    		{
	    			Logger.getLogger(getClass()).debug("sending daily repeated downtime alarm for user " + userid);
	    		}

				// TODO: take user's timezone from user profile and calculate time
				// and timezone offset to show in the mail
				// int tzHourOffset = XmlUserProfileManager.getInstance().getTimeZone(userid);
    			
				String tzone = getTimeZoneOffset(alarmCal.getTime().getTime());
    			
    			if (alarmDistributor.sendAlarmMail(userid, appointment, tzone, alarmCal.getTime()))
				{
					newEvent.setMailAlarmed();
					newEvent.setLastMailAlarmed(alarmCal.getTime());
					appointment.setMailAlarmed(true);
					appointment.setLastMailAlarmed(alarmCal.getTime());
					
					updateAppointment(userid, appointment, false);
					
		            downtimeAlarmSent = true;
				}

    			alarmCal.add(Calendar.DAY_OF_MONTH, 1);
	    	}
	    }
	    else if (appointment.getRepeatPeriod() == AlarmEntry.REPEAT_WEEKLY)
	    {
	    	alarmCal.add(Calendar.WEEK_OF_YEAR, 1);
	    	while (alarmCal.getTimeInMillis() < todayCal.getTimeInMillis())
	    	{
	    		if (Logger.getLogger(getClass()).isDebugEnabled())
	    		{
	    			Logger.getLogger(getClass()).debug("sending weekly repeated downtime alarm for user " + userid);
	    		}

    			// TODO: take user's timezone from user profile and calculate time
				// and timezone offset to show in the mail
				// int tzHourOffset = XmlUserProfileManager.getInstance().getTimeZone(userid);
    			
				String tzone = getTimeZoneOffset(alarmCal.getTime().getTime());

    			if (alarmDistributor.sendAlarmMail(userid, appointment, tzone, alarmCal.getTime()))
				{
					newEvent.setMailAlarmed();
					newEvent.setLastMailAlarmed(alarmCal.getTime());
					appointment.setMailAlarmed(true);
					appointment.setLastMailAlarmed(alarmCal.getTime());
					
					updateAppointment(userid, appointment, false);
					
					downtimeAlarmSent = true;
				}

		    	alarmCal.add(Calendar.WEEK_OF_YEAR, 1);
	    	}
	    }
	    else if (appointment.getRepeatPeriod() == AlarmEntry.REPEAT_MONTHLY)
	    {
	    	alarmCal.add(Calendar.MONTH, 1);
	    	while (alarmCal.getTimeInMillis() < todayCal.getTimeInMillis())
	    	{
	    		if (Logger.getLogger(getClass()).isDebugEnabled())
	    		{
	    			Logger.getLogger(getClass()).debug("sending monthly repeated downtime alarm for user " + userid);
	    		}

    			// TODO: take user's timezone from user profile and calculate time
				// and timezone offset to show in the mail
				// int tzHourOffset = XmlUserProfileManager.getInstance().getTimeZone(userid);
    			
				String tzone = getTimeZoneOffset(alarmCal.getTime().getTime());

    			if (alarmDistributor.sendAlarmMail(userid, appointment, tzone, alarmCal.getTime()))
				{
					newEvent.setMailAlarmed();
					newEvent.setLastMailAlarmed(alarmCal.getTime());
					appointment.setMailAlarmed(true);
					appointment.setLastMailAlarmed(alarmCal.getTime());
					
					updateAppointment(userid, appointment, false);
					
		            downtimeAlarmSent = true;
				}
	    		
		    	alarmCal.add(Calendar.MONTH, 1);
	    	}
	    }
	    else if (appointment.getRepeatPeriod() == AlarmEntry.REPEAT_ANNUAL)
	    {
	    	alarmCal.add(Calendar.YEAR, 1);
	    	while (alarmCal.getTimeInMillis() < todayCal.getTimeInMillis())
	    	{
	    		if (Logger.getLogger(getClass()).isDebugEnabled())
	    		{
	    			Logger.getLogger(getClass()).debug("sending annual repeated downtime alarm for user " + userid);
	    		}

    			// TODO: take user's timezone from user profile and calculate time
				// and timezone offset to show in the mail
				// int tzHourOffset = XmlUserProfileManager.getInstance().getTimeZone(userid);
    			
				String tzone = getTimeZoneOffset(alarmCal.getTime().getTime());

    			if (alarmDistributor.sendAlarmMail(userid, appointment, tzone, alarmCal.getTime()))
				{
					newEvent.setMailAlarmed();
					newEvent.setLastMailAlarmed(alarmCal.getTime());
					appointment.setMailAlarmed(true);
					appointment.setLastMailAlarmed(alarmCal.getTime());
					
					updateAppointment(userid, appointment, false);
					
		            downtimeAlarmSent = true;
				}
	    		
		    	alarmCal.add(Calendar.YEAR, 1);
	    	}
	    }
	    else if (appointment.getRepeatPeriod() == AlarmEntry.REPEAT_WEEKDAY)
	    {
	    	if (alarmCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
	    	{
		    	alarmCal.add(Calendar.DAY_OF_MONTH, 3);
	    	}
	    	else if (alarmCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
	    	{
		    	alarmCal.add(Calendar.DAY_OF_MONTH, 2);
	    	}
	    	else
	    	{
		    	alarmCal.add(Calendar.DAY_OF_MONTH, 1);
	    	}
	    	while (alarmCal.getTimeInMillis() < todayCal.getTimeInMillis())
	    	{
	    		if (Logger.getLogger(getClass()).isDebugEnabled())
	    		{
	    			Logger.getLogger(getClass()).debug("sending weekday repeated downtime alarm for user " + userid + " alarm time: " + alarmCal.getTime());
	    		}

    			// TODO: take user's timezone from user profile and calculate time
				// and timezone offset to show in the mail
				// int tzHourOffset = XmlUserProfileManager.getInstance().getTimeZone(userid);
    			
				String tzone = getTimeZoneOffset(alarmCal.getTime().getTime());

    			if (alarmDistributor.sendAlarmMail(userid, appointment, tzone, alarmCal.getTime()))
				{
					newEvent.setMailAlarmed();
					newEvent.setLastMailAlarmed(alarmCal.getTime());
					appointment.setMailAlarmed(true);
					appointment.setLastMailAlarmed(alarmCal.getTime());
					
					updateAppointment(userid, appointment, false);
					
		            downtimeAlarmSent = true;
				}
	    		
    	    	if (alarmCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
    	    	{
    		    	alarmCal.add(Calendar.DAY_OF_MONTH, 3);
    	    	}
    	    	else if (alarmCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
    	    	{
    		    	alarmCal.add(Calendar.DAY_OF_MONTH, 2);
    	    	}
    	    	else
    	    	{
    		    	alarmCal.add(Calendar.DAY_OF_MONTH, 1);
    	    	}
	    	}
	    }
	    
		newEvent.setEventDate(alarmCal.getTime());
		newEvent.setAlarmTime(new Date(alarmCal.getTimeInMillis() - alarmOffset));
	    
		newEvent.unsetMailAlarmed();
		
    	alarmIdx.moveEvent(newEvent, oldEventTime);    	
	    
	    return downtimeAlarmSent;
	}
    
    public AlarmIndex getAlarmIndex()
    {
    	return alarmIdx;
    }
}

