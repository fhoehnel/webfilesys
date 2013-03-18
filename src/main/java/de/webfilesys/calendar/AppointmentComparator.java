package de.webfilesys.calendar;

import java.util.*;

/**
 * Comparator for Tasks
 *
 */
public class AppointmentComparator
implements Comparator
{
    public AppointmentComparator()
    {
    }

    public int compare(Object o1,Object o2)
    {
        if (!o2.getClass().equals(o1.getClass()))
        {
            throw new ClassCastException();
        }

        Appointment appointment1=(Appointment) o1;
        Appointment appointment2=(Appointment) o2;

        if ((appointment1==null) || (appointment2==null))
        {
            return(0);
        }

        if (appointment1.getEventTime()==null)
        {
            if (appointment2.getEventTime()==null)
            {
                return(0);
            }
            else
            {
                return(-1);
            }
        }
        else
        {
            if (appointment2.getEventTime()==null)
            {
                return(1);
            }
        }

        if (appointment1.getEventTime().getTime() < appointment2.getEventTime().getTime())
        {
            return(-1);
        }
        else
        {
            if (appointment1.getEventTime().getTime() > appointment2.getEventTime().getTime())
            {
                return(1);
            }
            else
            {
                return(0);
            }
        }
    }

    public boolean equals(Object obj)
    {
        return(obj.equals(this));
    }
}


