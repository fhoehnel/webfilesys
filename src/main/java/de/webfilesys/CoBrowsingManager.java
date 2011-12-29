package de.webfilesys;

import java.util.HashMap;

/** 
 * Handles coordination of co-browsing sessions.
 * @author Frank Hoehnel
 */
public class CoBrowsingManager {

	private static CoBrowsingManager instance = null;
	
    private HashMap<String, String> prefetchImageMap = null;
    private HashMap<String, String> currentImageMap = null;
	
	private CoBrowsingManager()
	{
        prefetchImageMap = new HashMap<String, String>(5);        
        currentImageMap = new HashMap<String, String>(5);        
	}
	
	public static CoBrowsingManager getInstance()
	{
		if (instance == null) {
			instance = new CoBrowsingManager();
		}
		return instance;
	}
	
    public void setCoBrowsingImage(String userid, String imgPath)
    {
        String prefetchImgPath = (String) prefetchImageMap.get(userid);
        
        if (prefetchImgPath != null) {
            currentImageMap.put(userid, prefetchImgPath);
        } else {
            currentImageMap.put(userid, imgPath);
        }
        
        prefetchImageMap.put(userid, imgPath);
    }
    
    public void terminateCoBrowsing(String userid)
    {
        prefetchImageMap.remove(userid);
        currentImageMap.remove(userid);
    }
    
    public String getCoBrowsingPrefetchImage(String userid)
    {
        return (String) prefetchImageMap.get(userid);
    }

    public String getCoBrowsingCurrentImage(String userid)
    {
        return (String) currentImageMap.get(userid);
    }
}
