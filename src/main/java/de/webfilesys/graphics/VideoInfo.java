package de.webfilesys.graphics;

public class VideoInfo {
    
    private int width;
    
    private int height;
    
    private int frameRate;
    
    private String codec;
    
    private String duration;
    
    private int durationSeconds;
    
    private int ffprobeResult;
    
    private boolean ffprobeEmptyOutput = false;

    public void setWidth(int newVal) {
        width = newVal;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setHeight(int newVal) {
        height = newVal;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setFrameRate(int newVal) {
        frameRate = newVal;
    }
    
    public int getFrameRate() {
        return frameRate;
    }
    
    public void setCodec(String newVal) {
        codec = newVal;
    }
    
    public String getCodec() {
        return codec;
    }
    
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
