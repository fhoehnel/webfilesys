package de.webfilesys.unix;

import java.io.*;
import java.util.*;

/**
 *  For old Linux (< Suse 6.4) wich does not support ps -eo user,pid,.......
 */
public class OldLinuxProcessTree extends ProcessTree
{
    public OldLinuxProcessTree(String userid)
    {
        super(userid);
    }

    protected void readProcessList()
    {
        String prog_name_parms[];
     
        prog_name_parms=new String[3];

        prog_name_parms[0]=new String("/bin/sh");
        prog_name_parms[1]=new String("-c");

        prog_name_parms[2]=new String("ps auxlww --sort=uid,pid" + " 2>&1");

        Runtime rt=Runtime.getRuntime();

        Process dsmc_process=null;

        try
        {
            dsmc_process=rt.exec(prog_name_parms);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        DataInputStream dsmc_out=new DataInputStream(dsmc_process.getInputStream());
        String stdout_line=null;

        StringTokenizer proc_parser=null;

        String pid_string=null;
        String size_string;
        boolean done=false;
        boolean first_line=true;
        String userid=null;
        String cmd_str;
        String cpu_time;
        String tty;
        String start_time;
        String ppid;
        int cmdStartIndex=0;
        int ttyStartIndex=0;
        int timeStartIndex=0;

        while (!done)
        {
            try 
            {
                stdout_line = dsmc_out.readLine();
            }
            catch (IOException ioe)
            {
                System.out.println(ioe);
            }
              
            if (stdout_line==null)
            {
                done=true;
            }
            else
            {
                // System.out.println(stdout_line);
                             
                proc_parser=new StringTokenizer(stdout_line);
             
                if (first_line)
                {
                    cmdStartIndex=stdout_line.indexOf("COMMAND");
                    ttyStartIndex=stdout_line.indexOf("TTY");
                    timeStartIndex=stdout_line.indexOf("TIME");
               
                    first_line=false;
                }
                else
                {   proc_parser=new StringTokenizer(stdout_line);
                 
                    proc_parser.nextToken();
                    userid=proc_parser.nextToken();
                    pid_string=proc_parser.nextToken();
                    ppid=proc_parser.nextToken(); 
                    proc_parser.nextToken();
                    proc_parser.nextToken();
                    size_string=proc_parser.nextToken();
                    tty=stdout_line.substring(ttyStartIndex,ttyStartIndex+3);
                    cpu_time=stdout_line.substring(timeStartIndex,timeStartIndex+5);
                    cmd_str=stdout_line.substring(cmdStartIndex);
                     
                    if (forUserid.equals("all") || forUserid.equals(userid))
                    {
                        UnixProcess newProcess=new UnixProcess();
                        newProcess.setUID(userid);
                        newProcess.setPid(pid_string);
                        newProcess.setPPid(ppid);
                        newProcess.setSize(size_string);
                        newProcess.setTTY(tty);
                        newProcess.setCPUTime(cpu_time);
                        newProcess.setCmd(cmd_str); 
                     
                        addProcessToTree(newProcess);
                    }
                }
            }
        }     
    }

}