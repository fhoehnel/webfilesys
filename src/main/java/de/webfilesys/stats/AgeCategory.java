package de.webfilesys.stats;

public class AgeCategory extends StatisticsCategory {
	private long ageInMillis;
	private String displayText;
	
	public AgeCategory(long age, String ageDisplayText) {
        super();
	    ageInMillis = age;
		displayText = ageDisplayText;
	}
	
	public long getAgeInMillis() {
		return ageInMillis;
	}
	
	public String getDisplayText() {
	    return displayText;
	}
}
