package de.webfilesys.graphics;

public class AudioInfo {
    
    private String duration;
    
    private int durationSeconds;
    
    private int ffprobeResult;
    
    private boolean ffprobeEmptyOutput = false;

    public void setDuration(String newVal) {
        duration = newVal;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public void setDurationSeconds(int newVal) {
    	durationSeconds = newVal;
    }
    
    public int getDurationSeconds() {
    	return durationSeconds;
    }
    
    public void setFfprobeResult(int newVal) {
    	ffprobeResult = newVal;
    }
    
    public int getFfprobeResult() {
    	return ffprobeResult;
    }
    
    public void setFfprobeEmptyOutput(boolean newVal) {
    	ffprobeEmptyOutput = newVal;
    }
    
    public boolean isFfprobeEmptyOutput() {
    	return ffprobeEmptyOutput;
    }
}
