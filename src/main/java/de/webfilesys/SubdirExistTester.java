package de.webfilesys;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import de.webfilesys.graphics.ThumbnailThread;

public class SubdirExistTester extends Thread {
	private final ArrayList<QueueElem> queue;

	boolean shutdownFlag;

	private static SubdirExistTester instance = null;

	private SubdirExistTester() {
		queue = new ArrayList<>();
		shutdownFlag = false;
	}

	public static boolean instanceCreated() {
		return (instance != null);
	}

	public static SubdirExistTester getInstance() {
		if (instance == null) {
			instance = new SubdirExistTester();
			instance.start();
		}
		return (instance);
	}

	public void run() {
		LogManager.getLogger(getClass()).info("SubdirExistTester started");
		Thread.currentThread().setPriority(1);

		while (!shutdownFlag) {
			while (!queue.isEmpty()) {
				QueueElem elem = queue.get(0);
				testForExistingSubdirs(elem);
				synchronized (queue) {
					queue.remove(0);
				}
			}
			try {
				synchronized (this) {
					wait();
				}
			} catch (InterruptedException intEx) {
				shutdownFlag = true;
			}
		}
		LogManager.getLogger(getClass()).info("SubdirExistTester shutting down");
	}

	public synchronized void queuePath(String path, int scope, boolean forceRescan) {
		synchronized (queue) {
			queue.add(new QueueElem(path, scope, forceRescan));
		}
		notify();
	}

	private void testForExistingSubdirs(QueueElem queueElem) {
		String path = queueElem.getPath();
		Integer subdirExist = SubdirExistCache.getInstance().existsSubdir(path);

		if (queueElem.isForceRescan() || (subdirExist == null)) {
	        File rootDir = new File(path);
	        File[] rootFileList = rootDir.listFiles();
	        if (rootFileList != null) {
                boolean hasSubdirs = Arrays.stream(rootFileList)
                        .anyMatch(file -> file.isDirectory() && !file.getName().equals(ThumbnailThread.THUMBNAIL_SUBDIR));
	            if (hasSubdirs) {
	             	SubdirExistCache.getInstance().setExistsSubdir(path, 1);
	            } else {
	            	SubdirExistCache.getInstance().setExistsSubdir(path, 0);
	            }
	        } else {
	        	// TODO: set ExistsSubdir to null ?
	        	SubdirExistCache.getInstance().setExistsSubdir(path, 0);
	        }
		}
	}

	public class QueueElem {
		private String path;

		private int scope;

		private boolean forceRescan;
		
		public QueueElem(String path, int scope, boolean forceRescan) {
			this.path = path;
			this.scope = scope;
			this.forceRescan = forceRescan;
		}

		public String getPath() {
			return (path);
		}

		public int getScope() {
			return (scope);
		}
		
		public boolean isForceRescan() {
			return forceRescan;
		}
	}
}
