package de.webfilesys;

/**
 * @author Frank Hoehnel
 */
public interface Constants
{
    public static final int MAX_FILE_NUM = 4096;

	public static final int VIEW_MODE_LIST       = 1;
	public static final int VIEW_MODE_THUMBS     = 2;
	public static final int VIEW_MODE_STORY      = 3;
	public static final int VIEW_MODE_SLIDESHOW  = 4;
	public static final int VIEW_MODE_STATS      = 5;

    public static final int DEFAULT_SCREEN_WIDTH  = 1024;
    public static final int DEFAULT_SCREEN_HEIGHT = 768;
	
	/** max length of img alt tag text that can be displayed */
	public static final int MAX_ALT_TAG_LENGTH = 254;  
	
	/** default number of files per page in the list view */
	public static final int DEFAULT_LIST_PAGE_SIZE = 20;
	
	public static final String imgFileMasks[]={"*.gif","*.jpg","*.jpeg","*.png","*.bmp"};

    public static final String JPEG_FILE_MASKS[]={"*.jpg","*.jpeg"};
	
	public static final String UPLOAD_COUNTER = "uploadCounter";
	public static final String UPLOAD_SIZE = "uploadSize";
	public static final String UPLOAD_SUCCESS = "uploadSuccess";
	public static final String UPLOAD_LIMIT_EXCEEDED = "uploadLimitExceeded";
	public static final String UPLOAD_CANCELED = "uploadCanceled";
	
	/** 
	 * Size in pixels of the larger dimension of picture thumbnails.
	 * The thumbnails contained in the EXIF data of pictures from digital cameras
	 * are usually 160 x 120 pix.
	 */
	public static int THUMBNAIL_SIZE = 160;

}
