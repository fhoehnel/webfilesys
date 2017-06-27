package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import org.apache.log4j.Logger;

public class FastPathQueue {
	public static final String FAST_PATH_DIR = "fastpath";

	private String fastPathFileName = null;

	static final int MAX_QUEUE_SIZE = 25;

	private ArrayList<String> pathQueue = null;

	FastPathQueue(String userid) {
		fastPathFileName = WebFileSys.getInstance().getConfigBaseDir() + "/" + FAST_PATH_DIR + "/" + userid + ".dat";

		if (!loadFromFile()) {
			pathQueue = new ArrayList<String>(MAX_QUEUE_SIZE);
		}
	}

	private boolean loadFromFile() {

		boolean success = false;
		
		ObjectInputStream fastPathFile = null;

		try {
			fastPathFile = new ObjectInputStream(new FileInputStream(fastPathFileName));
			pathQueue = (ArrayList<String>) fastPathFile.readObject();
			fastPathFile.close();
			success = true;
		} catch (ClassNotFoundException cnfe) {
			Logger.getLogger(getClass()).warn(cnfe);
		} catch (IOException ioe) {
			Logger.getLogger(getClass()).warn(ioe);
		} catch (ClassCastException cex) {
			Logger.getLogger(getClass()).warn(cex);
		} finally {
			if (fastPathFile != null) {
				try {
					fastPathFile.close();
				} catch (Exception ex) {
				}
			}
		}

		return (success);
	}

	public void saveToFile() {
		File fastPathDir = new File(WebFileSys.getInstance().getConfigBaseDir() + "/" + FAST_PATH_DIR);

		if (!fastPathDir.exists()) {
			if (!fastPathDir.mkdirs()) {
				Logger.getLogger(getClass()).warn("cannot create fastpath directory " + fastPathDir);
			}

			return;
		}

		ObjectOutputStream fastPathFile = null;

		try {
			fastPathFile = new ObjectOutputStream(new FileOutputStream(fastPathFileName));
			fastPathFile.writeObject(pathQueue);
			fastPathFile.flush();
		} catch (IOException ioEx) {
			Logger.getLogger(getClass()).warn(ioEx);
		} finally {
			if (fastPathFile != null) {
				try {
					fastPathFile.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	public synchronized void queuePath(String pathName) {
		// remove trailing separator char
		if (File.separatorChar == '/') {
			if ((pathName.length() > 1) && pathName.endsWith("/")) {
				pathName = pathName.substring(0, pathName.length() - 1);
			}
		} else {
			if ((pathName.length() > 3) && pathName.endsWith("\\")) {
				pathName = pathName.substring(0, pathName.length() - 1);
			}
		}

		pathQueue.remove(pathName);

		pathQueue.add(0, pathName);

		if (pathQueue.size() > MAX_QUEUE_SIZE) {
			pathQueue.remove(pathQueue.size() - 1);
		}
	}

	public ArrayList<String> getPathList() {
		return (pathQueue);
	}

}