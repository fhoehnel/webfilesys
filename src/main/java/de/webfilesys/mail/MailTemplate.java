package de.webfilesys.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Hashtable;

public class MailTemplate
{
    private File templateFile;

    private Hashtable varValues;

    StringBuffer replaceBuffer;


    private void init(File templateFile)
    throws IllegalArgumentException
    {
        if (!templateFile.canRead())
        {
            throw new IllegalArgumentException("MailTemplate: template file doesn't exist or is not readable: " + templateFile);
        }

        this.templateFile = templateFile;

        varValues = new Hashtable();
    }

    public MailTemplate(String templateFileName)
    throws IllegalArgumentException
    {
        if (templateFileName == null)
        {
            throw new IllegalArgumentException("MailTemplate: template file name is null");
        }

        init(new File(templateFileName));
    }

    public MailTemplate(File templateFile)
    throws IllegalArgumentException
    {
        init(templateFile);
    }

    /**
        Set the replacement text for a variable in the template.
        @param varName name of the variable
        @param varValue the replacement text for the variable
    */
    public void setVarValue(String varName,String varValue)
    {
        if ((varName==null) || (varName.length()==0) || (varValue==null))
        {
            return;
        }

        varValues.put(varName,varValue);
    }

    /**
        Read the template file, replace any variables for wich a replacement
        text has been defined and return the changed text.
    */
    public String getText()
    {
        replaceBuffer=new StringBuffer();

        FileInputStream fis = null;
        BufferedReader input = null;

        try
        {
            fis = new FileInputStream(templateFile);
            
            input = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            
            String line=null;

            while ( (line=input.readLine()) != null )
            {
                Enumeration allVarNames=varValues.keys();

                while (allVarNames.hasMoreElements())
                {
                    String actVar=(String) allVarNames.nextElement();
                    String varString="$" + actVar;

                    if (line.indexOf(varString)>=0)
                    {
                        String actValue=(String) varValues.get(actVar);
                        line=replaceVar(line,varString,actValue);
                    }

                }

                replaceBuffer.append(line);
                replaceBuffer.append("\n");
            }
        }
        catch (IOException ioex)
        {
            return(null);
        }
        finally 
        {
            if (fis != null) 
            {
                try
                {
                    input.close();
                    fis.close();
                }
                catch (Exception ex)
                {
                }
            }
        }

        return(replaceBuffer.toString());
    }

    /**
    *   Replace a variable by it's actual value.<br>
    *   @param line the line of text from the source template file
    *   @param varName name of the variable to be replaced
    *   @param replaceText text to be inserted in place of the variable
    *   @returns the modified text line
    */
    private String replaceVar(String line,String varName,String replaceText)
    {
        int idx=line.indexOf(varName);
        if (idx<0)
        {
            return(line);
        }

        if (replaceText==null)
        {
            replaceText="";
        }

        return(line.substring(0,idx) + replaceText + line.substring(idx+varName.length()));
    }

}
