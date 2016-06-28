package de.webfilesys.graphics;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class BlogThumbnailHandler {

	private static final Logger LOG = Logger.getLogger(BlogThumbnailHandler.class);
	
	public static final String BLOG_THUMB_PATH = "_thumbnails400";

	public static final int THUMBNAIL_SIZE = 400;
	
	private static BlogThumbnailHandler instance = null;
	
	public static BlogThumbnailHandler getInstance() {
		if (instance == null) {
			instance = new BlogThumbnailHandler();
		}
		return instance;
	}
	
	public void createBlogThumbnail(String origImgPath) {
		int lastSepIdx = origImgPath.lastIndexOf(File.separatorChar);
		
		String thumbnailPath = origImgPath.substring(0, lastSepIdx + 1) + BLOG_THUMB_PATH;
		boolean thumbnailDirOk = false;
		File thumbnailDirFile = new File(thumbnailPath);
		if (thumbnailDirFile.exists() && thumbnailDirFile.isDirectory()) {
			thumbnailDirOk = true;
		} else {
			if (thumbnailDirFile.mkdir()) {
				thumbnailDirOk = true;
			} else {
				Logger.getLogger(getClass()).error("failed to create directory for blog thumbnails");
			}
		}
		
		if (thumbnailDirOk) {
			String thumbnailFilePath = thumbnailPath + File.separator + origImgPath.substring(lastSepIdx + 1);

			long startTime = System.currentTimeMillis();
			
			if (ImageTransformUtil.createScaledImage(origImgPath, thumbnailFilePath, THUMBNAIL_SIZE, THUMBNAIL_SIZE)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("created blog thumbnail " + thumbnailFilePath + " in " + (System.currentTimeMillis() - startTime) + " ms");
				}
			} else {
				LOG.warn("failed to create blog thumbnail " + thumbnailFilePath);
			}
		}
	}
	
	public String getPathOfExistingThumbnail(String origImgPath) {
		int lastSepIdx = origImgPath.lastIndexOf(File.separatorChar);
		
		String thumbnailPath = origImgPath.substring(0, lastSepIdx + 1) + BLOG_THUMB_PATH;
		String thumbnailFilePath = thumbnailPath + File.separator + origImgPath.substring(lastSepIdx + 1);
		
		File thumbnailFile = new File(thumbnailFilePath);
		if (thumbnailFile.exists() && thumbnailFile.isFile() && thumbnailFile.canRead()) {
			return thumbnailFilePath;
		}
		return null;
	}
	
	public boolean deleteThumbnail(String origImgPath) {
		int lastSepIdx = origImgPath.lastIndexOf(File.separatorChar);
		
		String thumbnailPath = origImgPath.substring(0, lastSepIdx + 1) + BLOG_THUMB_PATH;
		String thumbnailFilePath = thumbnailPath + File.separator + origImgPath.substring(lastSepIdx + 1);
		
		File thumbnailFile = new File(thumbnailFilePath);
		if (thumbnailFile.exists() && thumbnailFile.isFile()) {
			return thumbnailFile.delete();
		}
		return false;
	}
	
	public boolean renameThumbnail(String origImgPath, String newName) {
		int lastSepIdx = origImgPath.lastIndexOf(File.separatorChar);
		
		String thumbnailPath = origImgPath.substring(0, lastSepIdx + 1) + BLOG_THUMB_PATH;
		String thumbnailFilePath = thumbnailPath + File.separator + origImgPath.substring(lastSepIdx + 1);
		
		File thumbnailFile = new File(thumbnailFilePath);
		if (thumbnailFile.exists() && thumbnailFile.isFile()) {
			String newThumbnailFilePath = thumbnailPath + File.separator + newName;
			File newThumbnailFile = new File(newThumbnailFilePath);
			return thumbnailFile.renameTo(newThumbnailFile);
		}
		return false;
	}
	
	public boolean rotateThumbnail(String origImgPath, String degrees) {
		int lastSepIdx = origImgPath.lastIndexOf(File.separatorChar);
		
		String thumbnailPath = origImgPath.substring(0, lastSepIdx + 1) + BLOG_THUMB_PATH;
		String thumbnailFilePath = thumbnailPath + File.separator + origImgPath.substring(lastSepIdx + 1);

		ImageTransform imgTrans = new ImageTransform(thumbnailFilePath, "rotate", degrees);

		if (imgTrans.execute(false) != null) {
			File oldThumbFile = new File(thumbnailFilePath);
			if (oldThumbFile.exists()) {
				return oldThumbFile.delete();
			} 
			return true;
		}
		return false;
	}
	
	public ScaledImage getThumbnailDimensions(String origImgPath) {
		int lastSepIdx = origImgPath.lastIndexOf(File.separatorChar);
		
		String thumbnailPath = origImgPath.substring(0, lastSepIdx + 1) + BLOG_THUMB_PATH;
		String thumbnailFilePath = thumbnailPath + File.separator + origImgPath.substring(lastSepIdx + 1);
		
		File thumbnailFile = new File(thumbnailFilePath);
		if (thumbnailFile.exists() && thumbnailFile.isFile() && thumbnailFile.canRead()) {
			try {
				ScaledImage thumbImg = new ScaledImage(thumbnailFilePath, 1000, 1000);	
				return thumbImg;
			} catch (IOException ioex) {
				LOG.error("could not determine dimensions of thumbnail for blog file " + origImgPath, ioex);
			}
		}
        return null;		
	}
}
