package de.webfilesys;

/**
 * An object of this class represents geographical coordinates (latitude, longitude in degrees) 
 * and a zoom factor for GeoTagging with Google Maps. 
 *
 * @author Frank Hoehnel
 */
public class GeoTag 
{
	private float latitude = 0.0f;
	
	private float longitude = 0.0f;
	
	private int zoomFactor = 11;
	
	private String infoText = null;
	
	public GeoTag()
	{
	}

	public GeoTag(float latitude, float longitude)
	{
		this.latitude = latitude;
		
		this.longitude = longitude;
	}
	
	public GeoTag(float latitude, float longitude, int zoomFactor)
	{
		this.latitude = latitude;
		
		this.longitude = longitude;
		
		this.zoomFactor = zoomFactor;
	}
	
	public GeoTag(String latitudeStr, String longitudeStr)
	{
        try
        {
        	latitude = Float.parseFloat(latitudeStr);
        	
        	longitude = Float.parseFloat(longitudeStr);
        }
        catch (NumberFormatException nfex)
        {
        }
	}
	
	public GeoTag(String latitudeStr, String longitudeStr, int zoomFactor)
	{
        try
        {
        	latitude = Float.parseFloat(latitudeStr);
        	
        	longitude = Float.parseFloat(longitudeStr);
        }
        catch (NumberFormatException nfex)
        {
        }

        this.zoomFactor = zoomFactor;
	}
	
	public GeoTag(String latitudeStr, String longitudeStr, String zoomFactorStr)
	{
        try
        {
        	latitude = Float.parseFloat(latitudeStr);
        	
        	longitude = Float.parseFloat(longitudeStr);
        	
        	zoomFactor = Integer.parseInt(zoomFactorStr);
        }
        catch (NumberFormatException nfex)
        {
        }
	}
	
	public void setLatitude(float newVal)
	{
	    latitude = newVal;	
	}

	public void setLatitude(String newVal)
	{
        try
        {
        	latitude = Float.parseFloat(newVal);
        }
        catch (NumberFormatException nfex)
        {
        }
	}
	
	public float getLatitude()
	{
		return(latitude);
	}

	public void setLongitude(float newVal)
	{
	    longitude = newVal;	
	}

	public void setLongitude(String newVal)
	{
        try
        {
        	longitude = Float.parseFloat(newVal);
        }
        catch (NumberFormatException nfex)
        {
        }
	}
	
	public float getLongitude()
	{
		return(longitude);
	}
	
	public void setZoomFactor(int newVal)
	{
		zoomFactor = newVal;
	}
	
    public int getZoomFactor()
    {
    	return(zoomFactor);
    }
    
    public void setInfotext(String newVal)
    {
    	infoText = newVal;
    }
    
    public String getInfoText()
    {
    	return(infoText);
    }
}
