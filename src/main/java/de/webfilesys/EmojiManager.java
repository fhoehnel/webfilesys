package de.webfilesys;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.log4j.Logger;

public class EmojiManager {
    public static final String EMOJI_DIR = "emoticons";
	
    private static EmojiManager emojiMgr = null;
    
    private ArrayList<String> emoticons = null;

    public static synchronized EmojiManager getInstance() {
        if (emojiMgr == null) {
        	emojiMgr = new EmojiManager();
        }

        return(emojiMgr);
    }

    private EmojiManager() {
        readEmoticons();
    }
    
    private void readEmoticons() {
    	String emojiFolderPath = WebFileSys.getInstance().getWebAppRootDir() + EMOJI_DIR;
    	
    	emoticons = new ArrayList<String>();
    	
    	File emojiFolder = new File(emojiFolderPath);
    	
    	if ((!emojiFolder.exists()) || (!emojiFolder.isDirectory()) || (!emojiFolder.canRead())) {
    		Logger.getLogger(getClass()).error("emoticons folder is not e readable directory: " + emojiFolderPath);
    		return;
    	}
    	
    	File[] emojiFiles = emojiFolder.listFiles();
    	
    	if (emojiFiles != null) {
    		for (File emojiFile : emojiFiles) {
    			if (emojiFile.getName().toLowerCase().endsWith(".png")) {
                    if (emojiFile.isFile() && emojiFile.canRead()) {
        				emoticons.add(emojiFile.getName());
                    }
    			}
    		}
    	}
    	
    	if (emoticons.size() > 1) {
    		Collections.sort(emoticons);
    	}
    }
    
    public ArrayList<String> getEmoticons() {
    	return emoticons;
    }

}
