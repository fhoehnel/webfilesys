package de.webfilesys.unix;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

public class ProcessTree
{
    private Hashtable processList=null;

    private UnixProcess rootProcess=null;

    String forUserid=null;

    private StringBuffer outBuffer;
 
    public ProcessTree(String userid)
    {
        forUserid=userid;
        processList = new Hashtable();
        readProcessList();
    }

    // must be overwritten by derived classes
    protected void readProcessList()
    {
    }


    protected void addProcessToTree(UnixProcess newProcess)
    {
        String pid=newProcess.getPid();

        UnixProcess existingProcess=(UnixProcess) processList.get(pid);

        if (existingProcess!=null)
        {
            newProcess.childList=existingProcess.childList;
            processList.put(pid,newProcess);

            if ((rootProcess==null) || (existingProcess.getPid()==rootProcess.getPid()))
            {
                rootProcess=newProcess;
            }
        }

        String ppid=newProcess.getPPid();

        UnixProcess parent=(UnixProcess) processList.get(ppid);

        if (parent==null)
        {
            // System.out.println("adding placeholder parent " + ppid);
            parent=new UnixProcess();
            parent.setPid(ppid);
            processList.put(ppid,parent);
        }

        parent.addChild(newProcess);
        processList.put(pid,newProcess);
        if (rootProcess==null)
        {
            rootProcess=newProcess;
        }
        else
        {
            if (rootProcess.getPid()==newProcess.getPid())
            {
                rootProcess=parent;
            }
        }

    }

    public String toString()
    {
        if (rootProcess==null)
        {
            // System.out.println("root process is null");
        }

        outBuffer=new StringBuffer(); 

        treeToString(rootProcess,0);

        return(outBuffer.toString());
    }
 
    private void treeToString(UnixProcess actProcess,int level) 
    {
        for (int i=0;i<level;i++)
        {
             outBuffer.append("  ");
        }
        outBuffer.append(actProcess.getPid());
        outBuffer.append("  ");
        outBuffer.append(actProcess.getCmd());
        outBuffer.append("\n");
        Vector childList = actProcess.getChildren();

        for (int j=0;j<childList.size();j++)
        {
            UnixProcess actChild=(UnixProcess) childList.elementAt(j);
            
            treeToString(actChild,level+1);
        }
    }

    public String toHTML(boolean allowKill)
    {
        if (rootProcess==null)
        {
            Logger.getLogger(getClass()).error("ProcessTree.toHTML(): rootProcess is null");
            return "";
        }
        outBuffer=new StringBuffer(); 

        outBuffer.append("<table class=\"processList\">\n");
        outBuffer.append("<tr>\n");
        
        if (allowKill)
        {
            outBuffer.append("<th class=\"processList\">op</th>");
        }
        outBuffer.append("<th class=\"processList\">PID</th>\n");
        outBuffer.append("<th class=\"processList\">UID</th>\n");
        outBuffer.append("<th class=\"processList\">CMD</th>\n");
        outBuffer.append("<th class=\"processList\">Start Time</th>\n");
        outBuffer.append("<th class=\"processList\">CPU Time</th>\n");
        outBuffer.append("<th class=\"processList\">TTY</th>\n");
        outBuffer.append("</tr>\n");

        treeToHTML(rootProcess,0,allowKill);

        outBuffer.append("</table>\n");

        return(outBuffer.toString());
    }
 
    private void treeToHTML(UnixProcess actProcess, int level, boolean allowKill) 
    {
        String rowClass = null;
        if (actProcess.getUID().equals("root") || actProcess.getUID().equals("0"))
        {
            rowClass = "processRowRoot";
        }
        else
        {
            rowClass = "processRowOtherUser";
        }

        outBuffer.append("<tr class=\"" + rowClass + "\">\n");

        if (allowKill)
        {
            outBuffer.append("<td class=\"processKill\"> <a href=\"javascript:confirmKill('" + actProcess.getPid() + "')\"><img align=\"center\" border=\"0\" src=\"images/redx2.gif\" alt=\"kill process\"></a></td>\n");
        }
   
        outBuffer.append("<td class=\"processPID\">" + actProcess.getPid() + "</td>");
        outBuffer.append("<td class=\"processUID\">" + actProcess.getUID() + "</td>");

        outBuffer.append("<td class=\"processCMD\">");

        for (int i=0;i<level;i++)
        {
             outBuffer.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        }

        outBuffer.append(actProcess.getCmd());
        outBuffer.append("</td>\n");

        String startTime=actProcess.getStartTime();
        if (startTime.length()==0)
        {
            outBuffer.append("<td class=\"processStartTime\">&nbsp;</td>");
        }
        else
        {
            outBuffer.append("<td class=\"processStartTime\">" + startTime + "</td>");
        }
        outBuffer.append("<td class=\"processCPUTime\">" + actProcess.getCPUTime() + "</td>");
        outBuffer.append("<td class=\"processTTY\">" + actProcess.getTTY() + "</td>");

        outBuffer.append("</tr>\n");

        Vector childList = actProcess.getChildren();

        for (int j=0;j<childList.size();j++)
        {
            UnixProcess actChild=(UnixProcess) childList.elementAt(j);
            
            treeToHTML(actChild,level+1,allowKill);
        }
    }

}