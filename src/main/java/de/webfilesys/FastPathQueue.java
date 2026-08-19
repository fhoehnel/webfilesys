package de.webfilesys;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class FastPathQueue {
	public static final String FAST_PATH_DIR = "fastpath";

	private static final Gson gson = new Gson();

	private static final Type PATH_LIST_TYPE = new TypeToken<ArrayList<String>>() {}.getType();

	private String fastPathFileName = null;

	static final int MAX_QUEUE_SIZE = 25;

	private ArrayList<String> pathQueue = null;

	private boolean dirty = false;

	FastPathQueue(String userid) {
		fastPathFileName = WebFileSys.getInstance().getConfigBaseDir() + "/" + FAST_PATH_DIR + "/" + userid + ".json";

		if (!loadFromFile()) {
			pathQueue = new ArrayList<String>(MAX_QUEUE_SIZE);
		}
	}

	private boolean loadFromFile() {

		File fastPathFile = new File(fastPathFileName);

		if ((!fastPathFile.exists()) || (!fastPathFile.canRead())) {
			return false;
		}

		boolean success = false;

		InputStreamReader reader = null;

		try {
			reader = new InputStreamReader(new FileInputStream(fastPathFile), StandardCharsets.UTF_8);
			pathQueue = gson.fromJson(reader, PATH_LIST_TYPE);
			if (pathQueue != null) {
				success = true;
			}
		} catch (Exception ex) {
			LogManager.getLogger(getClass()).warn("failed to load fastpath file " + fastPathFileName, ex);
		} finally {
			if (reader != null) {
				try {
					reader.close();
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
				LogManager.getLogger(getClass()).warn("cannot create fastpath directory " + fastPathDir);
			}

			return;
		}

		OutputStreamWriter jsonOutFile = null;

		try {
			jsonOutFile = new OutputStreamWriter(new FileOutputStream(fastPathFileName), StandardCharsets.UTF_8);
			gson.toJson(pathQueue, jsonOutFile);
			jsonOutFile.flush();
			setDirty(false);
		} catch (IOException ioEx) {
			LogManager.getLogger(getClass()).warn(ioEx);
		} finally {
			if (jsonOutFile != null) {
				try {
					jsonOutFile.close();
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

		dirty = true;
	}

	public ArrayList<String> getPathList() {
		return (pathQueue);
	}

	public synchronized boolean isDirty() {
		return dirty;
	}

	public synchronized void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

}