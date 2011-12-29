package de.webfilesys.viewhandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.java2html.converter.JavaSource2HTMLConverter;
import de.java2html.javasource.JavaSource;
import de.java2html.javasource.JavaSourceParser;
import de.java2html.options.JavaSourceConversionOptions;
import de.webfilesys.ViewHandlerConfig;

/**
 * Formats Java source files to HTML using the Java2HTML library.
 * @author Frank Hoehnel
 */
public class JavaSourceViewHandler implements ViewHandler
{
    public void process(String filePath, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req, HttpServletResponse resp)
    {
        try 
        {
            PrintWriter output = resp.getWriter();

            output.println("<html>");
            output.println("<head>");
            output.println("</head>");
            output.println("<body>");

            FileReader javaSourceReader = new FileReader(new File(filePath));
                	
            JavaSource source = new JavaSourceParser().parse(javaSourceReader);
                
            JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
                
            converter.convert(source, JavaSourceConversionOptions.getDefault(), output);

            javaSourceReader.close();

            output.println("</body>");
            output.println("</html>");

            output.flush();
        } 
        catch (FileNotFoundException e)
        {
            Logger.getLogger(getClass()).error("Java to HTML conversion failed: " + e);
               
            return;
        }
        catch (IOException e) 
        {
            Logger.getLogger(getClass()).error("Java to HTML conversion failed: " + e);
               
            return;
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
            PrintWriter output = resp.getWriter();

            output.println("<html>");
            output.println("<head>");
            output.println("</head>");
            output.println("<body>");

            InputStreamReader javaSourceReader = new InputStreamReader(zipIn);
                    
            JavaSource source = new JavaSourceParser().parse(javaSourceReader);
                
            JavaSource2HTMLConverter converter = new JavaSource2HTMLConverter();
                
            converter.convert(source, JavaSourceConversionOptions.getDefault(), output);

            javaSourceReader.close();

            output.println("</body>");
            output.println("</html>");

            output.flush();
        } 
        catch (FileNotFoundException e)
        {
            Logger.getLogger(getClass()).error("Java to HTML conversion failed: " + e);
               
            return;
        }
        catch (IOException e) 
        {
            Logger.getLogger(getClass()).error("Java to HTML conversion failed: " + e);
               
            return;
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
