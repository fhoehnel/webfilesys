package de.webfilesys.viewhandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

import de.webfilesys.ViewHandlerConfig;

/**
 * Decompiles Java class files and forwards the generated Java source code to
 * the JavaSourceViewHandler.
 * 
 * @author Frank Hoehnel
 */
public class JavaClassProcyonViewHandler implements ViewHandler {
	public void process(String filePath, ViewHandlerConfig viewHandlerConfig, HttpServletRequest req,
			HttpServletResponse resp) {
		try {
			File tempFile = File.createTempFile("webfilesys", null);

			String tempDir = tempFile.getParent();

			File classFile = new File(filePath);

			String className = classFile.getName();

			if (className.indexOf('.') > 0) {
				className = className.substring(0, className.lastIndexOf('.'));
			}

			String javaFileName = className + ".java";

			String javaSourcePath = tempDir + File.separator + javaFileName;

			try {
				FileWriter javaSourceOut = new FileWriter(javaSourcePath);

				PlainTextOutput out = new PlainTextOutput(javaSourceOut);

				DecompilerSettings settings = new DecompilerSettings();

				Decompiler.decompile(filePath, out, settings);

				javaSourceOut.flush();

				javaSourceOut.close();
			} catch (Exception ex) {
				LogManager.getLogger(getClass()).error(ex);
			}

			(new JavaSourceViewHandler()).process(javaSourcePath, viewHandlerConfig, req, resp);

			tempFile.delete();

			File javaSourceTempFile = new File(javaSourcePath);

			if (javaSourceTempFile.exists()) {
				javaSourceTempFile.delete();
			}
		} catch (IOException ioex) {
			LogManager.getLogger(getClass()).error(ioex);
		}
	}

	/**
	 * Create the HTML response for viewing the given file contained in a ZIP
	 * archive..
	 * 
	 * @param zipFilePath path of the ZIP entry
	 * @param zipIn       the InputStream for the file extracted from a ZIP archive
	 * @param req         the servlet request
	 * @param resp        the servlet response
	 */
	public void processZipContent(String zipFilePath, InputStream zipIn, ViewHandlerConfig viewHandlerConfig,
			HttpServletRequest req, HttpServletResponse resp) {
		try {
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

			while ((bytesRead = zipIn.read(buff)) > 0) {
				classTempOut.write(buff, 0, bytesRead);
			}

			classTempOut.close();

			String javaSourcePath = tempDir + File.separator + fileName + ".java";

			try {
				FileWriter javaSourceOut = new FileWriter(javaSourcePath);

				PlainTextOutput out = new PlainTextOutput(javaSourceOut);

				DecompilerSettings settings = new DecompilerSettings();

				Decompiler.decompile(classTempFilePath, out, settings);

				javaSourceOut.flush();

				javaSourceOut.close();
			} catch (Exception ex) {
				LogManager.getLogger(getClass()).error(ex);
			}

			(new JavaSourceViewHandler()).process(javaSourcePath, viewHandlerConfig, req, resp);

			classTempFile.delete();

			File javaSourceTempFile = new File(javaSourcePath);

			if (javaSourceTempFile.exists()) {
				javaSourceTempFile.delete();
			}

			tempFile.delete();
		} catch (IOException ioex) {
			LogManager.getLogger(getClass()).error(ioex);
		}
	}

	/**
	 * Does this ViewHandler support reading the file from an input stream of a ZIP
	 * archive?
	 * 
	 * @return true if reading from ZIP archive is supported, otherwise false
	 */
	public boolean supportsZipContent() {
		return true;
	}
}
