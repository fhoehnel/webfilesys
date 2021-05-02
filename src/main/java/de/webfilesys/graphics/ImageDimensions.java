package de.webfilesys.graphics;

public class ImageDimensions {
	private int width;
	private int height;
	private int origWidth;
	private int origHeight;

	public ImageDimensions(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public void setOrigWidth(int newVal) {
		origWidth = newVal;
	}
	
	public int getOrigWidth() {
		return origWidth;
	}

	public void setOrigHeight(int newVal) {
		origHeight = newVal;
	}
	
	public int getOrigHeight() {
		return origHeight;
	}
}
