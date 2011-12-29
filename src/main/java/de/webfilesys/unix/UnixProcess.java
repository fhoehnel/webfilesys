package de.webfilesys.unix;

import java.util.*;

public class UnixProcess
{
    private String uid;
    private String pid;
    private String ppid;
    private String size;
    private String tty;
    private String cpuTime;
    private String startTime;
    private String cmdString;

    public Vector childList=null;

    public UnixProcess()
    {
        uid="";
        pid="";
        ppid="";
        size="";
        tty="";
        cpuTime="";
        startTime="";
        cmdString="";
        childList=new Vector();
    }

    public void addChild(UnixProcess newChild)
    {
        childList.addElement(newChild);
    }

    public Vector getChildren()
    {
        return(childList);
    }

    public void setUID(String uid)
    {
        this.uid=uid;
    }

    public void setPid(String pid)
    {
        this.pid=pid;
    }

    public void setPPid(String ppid)
    {
        this.ppid=ppid;
    }

    public void setSize(String size)
    {
        this.size=size;
    }

    public void setCPUTime(String cpuTime)
    {
        this.cpuTime=cpuTime;
    }

    public void setStartTime(String startTime)
    {
        this.startTime=startTime;
    }

    public void setTTY(String tty)
    {
        this.tty=tty;
    }

    public void setCmd(String cmdString)
    {
        this.cmdString=cmdString;
    }


    public String getUID()
    {
        return(uid);
    }

    public String getPid()
    {
        return(pid);
    }

    public String getPPid()
    {
        return(ppid);
    }

    public String getSize()
    {
        return(size);
    }

    public String getCPUTime()
    {
        return(cpuTime);
    }

    public String getStartTime()
    {
        return(startTime);
    }

    public String getTTY()
    {
        return(tty);
    }

    public String getCmd()
    {
        return(cmdString);
    }

}
