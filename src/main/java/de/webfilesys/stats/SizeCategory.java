package de.webfilesys.stats;

public class SizeCategory extends StatisticsCategory {
	private long minSize;
	private long maxSize;
	
	public SizeCategory(long minValue, long maxValue) {
        super();
	    minSize = minValue;
		maxSize = maxValue;
	}
	
	public long getMinSize() {
		return minSize;
	}
	
	public long getMaxSize() {
		return maxSize;
	}
}
