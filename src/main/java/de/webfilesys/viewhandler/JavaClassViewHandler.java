package de.webfilesys.viewhandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.webfilesys.ViewHandlerConfig;
import de.webfilesys.WebFileSys;

/**
 * Decompiles Java class files and forwards the generated Java source code 
 * to the JavaSourceViewHandler.
 * 
 * @author Frank Hoehnel
 */
public class JavaClassViewHandler implements ViewHandler
{
    public void process(String filePath, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req, HttpServletResponse resp)
    {
        try 
        {
            File tempFile = File.createTempFile("webfilesys", null);
            
            String tempDir = tempFile.getParent();
            
            File classFile = new File(filePath);
            
            String className = classFile.getName();
            
            if (className.indexOf('.') > 0) 
            {
                className = className.substring(0, className.lastIndexOf('.'));                
            }
            
            String javaFileName = className + ".java";
            
            String jadPath = WebFileSys.getInstance().getConfigBaseDir() + File.separator + "jad.exe";
            
            Runtime rt = Runtime.getRuntime();

            Process decompileProcess = null;

            String execString = jadPath + " -o -sjava -d " + tempDir + " " + filePath;

            try
            {
                decompileProcess = rt.exec(execString);
            }
            catch (Exception e)
            {
                Logger.getLogger(getClass()).error(e);
                return;
            }

            try
            {
                decompileProcess.waitFor();
            }
            catch (InterruptedException iex)
            {
                Logger.getLogger(getClass()).error(iex);
                return;
            }

            if (decompileProcess.exitValue()!=0)
            {
                Logger.getLogger(getClass()).error("exit value from jad: " + decompileProcess.exitValue());
                return;
            }
            
            String javaSourcePath = tempDir + File.separator + javaFileName;
            
            (new JavaSourceViewHandler()).process(javaSourcePath, viewHandlerConfig, req, resp);
            
            tempFile.delete();
            
            File javaSourceTempFile = new File(javaSourcePath);
            
            if (javaSourceTempFile.exists())
            {
                javaSourceTempFile.delete();
            }
        }
        catch (IOException ioex) 
        {
            Logger.getLogger(getClass()).error(ioex);
        }
    }
    
    
    /**
     * Create the HTML response for viewing the given file contained in a ZIP archive..
     * 
     * @param zipFilePath path of the ZIP entry
     * @param zipIn the InputStream for the file extracted from a ZIP archive
     * @param req the servlet request
     * @param resp the servlet response
     */
    public void processZipContent(String zipFilePath, InputStream zipIn, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req, HttpServletResponse resp)
    {
        try 
        {
            File tempFile = File.createTempFile("webfilesys", null);
            
            String tempDir = tempFile.getParent();
            
            String fileName = zipFilePath.replace('\\', '/');
            
            if (fileName.indexOf('/') >= 0) {
                fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
            }
            
            fileName = fileName.substring(0, fileName.lastIndexOf('.'));
            
            File classTempFile = new File(tempDir, fileName + ".class"); 
            
            String classTempFilePath = classTempFile.getAbsolutePath();
            
            FileOutputStream classTempOut = new FileOutputStream(classTempFile);
            
            byte[] buff = new byte[4096];
            
            int bytesRead;
            
            while ((bytesRead = zipIn.read(buff)) > 0)
            {
                classTempOut.write(buff, 0, bytesRead);
            }
            
            classTempOut.close();
            
            String jadPath = WebFileSys.getInstance().getConfigBaseDir() + File.separator + "jad.exe";
            
            Runtime rt = Runtime.getRuntime();

            Process decompileProcess = null;

            String execString = jadPath + " -o -sjava -d " + tempDir + " " + classTempFilePath;

            try
            {
                decompileProcess = rt.exec(execString);
            }
            catch (Exception e)
            {
                Logger.getLogger(getClass()).error(e);
                return;
            }

            try
            {
                decompileProcess.waitFor();
            }
            catch (InterruptedException iex)
            {
                Logger.getLogger(getClass()).error(iex);
                return;
            }

            if (decompileProcess.exitValue() != 0)
            {
                Logger.getLogger(getClass()).error("exit value from jad: " + decompileProcess.exitValue());
                return;
            }
            
            String javaSourcePath = tempDir + File.separator + fileName + ".java";
            
            (new JavaSourceViewHandler()).process(javaSourcePath, viewHandlerConfig, req, resp);
            
            classTempFile.delete();
            
            File javaSourceTempFile = new File(javaSourcePath);
            
            if (javaSourceTempFile.exists())
            {
                javaSourceTempFile.delete();
            }

            tempFile.delete();
        }
        catch (IOException ioex) 
        {
            Logger.getLogger(getClass()).error(ioex);
        }
    }
    
    /**
     * Does this ViewHandler support reading the file from an input stream of a ZIP archive?
     * @return true if reading from ZIP archive is supported, otherwise false
     */
    public boolean supportsZipContent()
    {
        return true;
    }
}
