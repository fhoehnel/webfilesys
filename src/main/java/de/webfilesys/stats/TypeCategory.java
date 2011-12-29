package de.webfilesys.stats;

public class TypeCategory extends StatisticsCategory {
    private String fileExt = null;
	
	public TypeCategory(String ext) {
        super();
	    fileExt = ext;
	}

	public String getFileExt() {
		return fileExt;
	}
	
}
