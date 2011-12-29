package de.webfilesys.graphics;
import java.io.File;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.exif.GpsDirectory;

public class CameraExifData
{    
	public static final int ORIENTATION_UNKNOWN = (-1);
	public static final int ORIENTATION_LANDSCAPE = 1;
	public static final int ORIENTATION_PORTRAIT = 2;
	
    Metadata metadata=null;

    Directory exifDirectory=null;
    
    Directory gpsDirectory = null;
    
	private int thumbHeight = 0;
	private int thumbWidth = 0;

    public CameraExifData(String imgFileName)
    {
        File jpegFile=new File(imgFileName);

        try
        {
            Metadata metadata=new Metadata();
            
            new ExifReader(jpegFile).extract(metadata);

            exifDirectory=metadata.getDirectory(ExifDirectory.class);

            gpsDirectory = metadata.getDirectory(GpsDirectory.class);
        }
        catch (Exception ex)
        {
            Logger.getLogger(getClass()).warn("failed to extract exif data from file " + imgFileName + ": " + ex);
        }
    }  
  
    public boolean hasExifData()
    {
        if (exifDirectory == null) 
        {
            return false;    
        }
        
        return(exifDirectory.getTagCount() > 0);
    }

    public void printExifData()
    {
        if (exifDirectory==null)
        {
            System.out.println("exif directory is null");
            return;
        }
        
        try
        {
            Iterator tags=exifDirectory.getTagIterator();

            while (tags.hasNext())
            {                    
                Tag tag=(Tag) tags.next();

                System.out.print(tag.getTagType() + " , ");
                System.out.print(tag.getTagName() + " , ");

                try
                {
                    System.out.println(tag.getDescription());
                }
                catch (java.lang.NoSuchMethodError nsm)
                {
                    System.out.println(nsm);
                }
            }
        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }
    }

    public String getManufacturer()
    {
        if (exifDirectory==null)
        {
            return("");
        }

        return(exifDirectory.getString(ExifDirectory.TAG_MAKE));
    }

    public String getCameraModel()
    {
        if (exifDirectory==null)
        {
            return("");
        }

        return(exifDirectory.getString(ExifDirectory.TAG_MODEL));
    }

    public String getExposureTime()
    {
        if (exifDirectory==null)
        {
            return(null);
        }

        return(exifDirectory.getString(ExifDirectory.TAG_EXPOSURE_TIME));
    }

    public String getAperture()
    {
        if (exifDirectory==null)
        {
            return("");
        }

        return(exifDirectory.getString(ExifDirectory.TAG_FNUMBER));
    }
    
    public String getISOValue()
    {
        if (exifDirectory==null)
        {
            return("");
        }

        return(exifDirectory.getString(ExifDirectory.TAG_ISO_EQUIVALENT));
    }

    public Date getExposureDate()
    {
        if (exifDirectory==null)
        {
            return(null);
        }

        try
        {
            return(exifDirectory.getDate(ExifDirectory.TAG_DATETIME_ORIGINAL));
        }
        catch (MetadataException metex)
        {
            return(null);
        }
    }

    /**
     *     0x9209: ('Flash', {0:  'No',
     *                        1:  'Fired',
     *                        5:  'Fired (?)', # no return sensed
     *                        7:  'Fired (!)', # return sensed
     *                        9:  'Fill Fired',
     *                       13: 'Fill Fired (?)',
     *                       15: 'Fill Fired (!)',
     *                       16: 'Off',
     *                       24: 'Auto Off',
     *                       25: 'Auto Fired',
     *                       29: 'Auto Fired (?)',
     *                       31: 'Auto Fired (!)',
     *                       32: 'Not Available'}),
     */
    public int getFlashFired()
    {
        if (exifDirectory==null)
        {
            return(-1);
        }

        try
        {
            return(exifDirectory.getInt(ExifDirectory.TAG_FLASH));
        }
        catch (MetadataException metex)
        {
            return(-1);
        }
    }

    public int getImageWidth()
    {
        if (exifDirectory==null)
        {
            return(-1);
        }

        try
        {
            return(exifDirectory.getInt(ExifDirectory.TAG_EXIF_IMAGE_WIDTH));
        }
        catch (MetadataException metex)
        {
            return(-1);
        }
    }

    public int getImageHeigth()
    {
        if (exifDirectory==null)
        {
            return(-1);
        }

        try
        {
            return(exifDirectory.getInt(ExifDirectory.TAG_EXIF_IMAGE_HEIGHT));
        }
        catch (MetadataException metex)
        {
            return(-1);
        }
    }

    public int getThumbnailOffset()
    {
        if (exifDirectory==null)
        {
            return(-1);
        }

        try
        {
            return(exifDirectory.getInt(ExifDirectory.TAG_THUMBNAIL_OFFSET));
        }
        catch (MetadataException metex)
        {
            return(-1);
        }
    }

    public int getThumbnailLength()
    {
        if (exifDirectory==null)
        {
            return(-1);
        }

        try
        {
            return(exifDirectory.getInt(ExifDirectory.TAG_THUMBNAIL_LENGTH));
        }
        catch (MetadataException metex)
        {
            return(-1);
        }
    }

    public int getThumbnailWidth()
    {
        if (exifDirectory==null)
        {
            return(-1);
        }

        if (!exifDirectory.containsTag(ExifDirectory.TAG_THUMBNAIL_IMAGE_WIDTH))
        {
			return(-1);
        }

        try
        {
			return(exifDirectory.getInt(ExifDirectory.TAG_THUMBNAIL_IMAGE_WIDTH));
        }
        catch (MetadataException metex)
        {
            return(-1);
        }
    }

    public int getThumbnailHeight()
    {
        if (exifDirectory==null)
        {
            return(-1);
        }

		if (!exifDirectory.containsTag(ExifDirectory.TAG_THUMBNAIL_IMAGE_HEIGHT))
		{
			return(-1);
		}

        try
        {
            return(exifDirectory.getInt(ExifDirectory.TAG_THUMBNAIL_IMAGE_HEIGHT));
        }
        catch (MetadataException metex)
        {
            return(-1);
        }
    }

	public int getOrientation()
	{
		if (exifDirectory == null)
		{
			return(ORIENTATION_UNKNOWN);
		}

		if (!exifDirectory.containsTag(ExifDirectory.TAG_ORIENTATION))
		{
            Logger.getLogger(getClass()).debug("missing EXIF tag for picture orientation");
			
			return(ORIENTATION_UNKNOWN);
		}

		try
		{
			return(exifDirectory.getInt(ExifDirectory.TAG_ORIENTATION));
		}
		catch (MetadataException metex)
		{
			return(ORIENTATION_UNKNOWN);
		}
	}

    public byte[] getThumbnailData()
    {
        if (exifDirectory==null)
        {
            return(null);
        }

        try
        {
            return(exifDirectory.getByteArray(ExifDirectory.TAG_THUMBNAIL_DATA));
        }
        catch (MetadataException metex)
        {
            return(null);
        }
    }

    /**
     * Because the TAG_THUMBNAIL_IMAGE_HEIGHT and TAG_THUMBNAIL_IMAGE_WIDTH are not reliable,
     * the thumbnail dimensions are determined here from the thumnail image data
     */
    public void getThumbnailDimensions()
    {
		if (!exifDirectory.containsTag(ExifDirectory.TAG_THUMBNAIL_DATA))
		{
			Logger.getLogger(getClass()).debug("missing EXIF thumbnail data tag");
			
			return;
		}

		byte thumbData[] = null;

        try
        {
			thumbData = exifDirectory.getByteArray(ExifDirectory.TAG_THUMBNAIL_DATA);
        }
        catch (MetadataException metaEx)
        {
            Logger.getLogger(getClass()).warn(metaEx);
			return;
        }

        byte marker1 = (byte) 0xff;
		byte marker2 = (byte) 0xc2;
		byte marker3 = (byte) 0xc0;

        for (int i = 0; (i < thumbData.length - 9); i++)
        {
             if (thumbData[i] == marker1)
             {
             	if ((thumbData[i + 1] == marker2) || (thumbData[i + 1] == marker3))
             	{
					if ((thumbData[i + 2] == 0) && (thumbData[i + 3] == 17) && (thumbData[i + 4] == 8))
					{						
						int num = thumbData[i + 5] & 0xff;
						thumbHeight = (num << 8) + (thumbData[i + 6] & 0xff);

						num = thumbData[i + 7] & 0xff;
						thumbWidth = (num << 8) + (thumbData[i + 8] & 0xff);
						
						return;
					}
             	}
             }
        }
    }

    /**
     * Determine the orientation from the thumbnail image data because the EXIF orientaion tag
     * is not reliable
     * @return lanscape=1, portrait=2, unknown=0
     */
    public int getThumbOrientation()
    {
		if ((thumbHeight == 0) || (thumbWidth == 0))
		{
			getThumbnailDimensions();
		}
        
		if ((thumbHeight == 0) || (thumbWidth == 0))
		{
			return(ORIENTATION_UNKNOWN);
		}
        
		if (thumbHeight > thumbWidth)
		{
			return(ORIENTATION_PORTRAIT);
		}
        
		return(ORIENTATION_LANDSCAPE);
    }

    public int getThumbWidth()
    {
		if (thumbWidth == 0)
		{
			getThumbnailDimensions();
		}

		return(thumbWidth);
    }
    
    public int getThumbHeight()
    {
		if (thumbHeight == 0)
		{
			getThumbnailDimensions();
		}

		return(thumbHeight);
    }
    
    public float getGpsLatitude()
    {
        if (gpsDirectory == null)
        {
            return(-1.0f);
        }

        float latitude = (-1.0f);
   
        try 
        {
            if (gpsDirectory.containsTag(GpsDirectory.TAG_GPS_LATITUDE))
            {
                Rational[] latArr = gpsDirectory.getRationalArray(GpsDirectory.TAG_GPS_LATITUDE);
                
                if ((latArr != null) && (latArr.length > 0)) 
                {
                    // Grad
                    Rational latitudeRational  = latArr[0];
                    
                    latitude = latitudeRational.floatValue();
                    
                    if (latArr.length > 1) 
                    {
                        // Minuten
                        latitudeRational  = latArr[1];
                        
                        latitude += latitudeRational.floatValue() / 60.0f;
                        
                        if (latArr.length > 2) 
                        {
                            // Sekunden
                            latitudeRational  = latArr[2];
                            
                            latitude += latitudeRational.floatValue() / 60.0f / 60.0f;
                        }
                    }
                }
            }
        }
        catch (MetadataException mex) 
        {
            Logger.getLogger(getClass()).warn(mex);
        }
        
        return(latitude);
    }
    
    public String getGpsLatitudeRef()
    {
        if (gpsDirectory == null)
        {
            return("");
        }
        
        return gpsDirectory.getString(GpsDirectory.TAG_GPS_LATITUDE_REF);
    }

    public float getGpsLongitude()
    {
        if (gpsDirectory == null)
        {
            return(-1.0f);
        }

        float longitude = (-1.0f);
   
        try 
        {
            if (gpsDirectory.containsTag(GpsDirectory.TAG_GPS_LONGITUDE))
            {
                Rational[] longArr = gpsDirectory.getRationalArray(GpsDirectory.TAG_GPS_LONGITUDE);
                
                if ((longArr != null) && (longArr.length > 0))
                {
                    Rational longitudeRational  = longArr[0];
                    
                    longitude = longitudeRational.floatValue();

                    if (longArr.length > 1) 
                    {
                        // Minuten
                        longitudeRational  = longArr[1];
                        
                        longitude += longitudeRational.floatValue() / 60.0f;
                        
                        if (longArr.length > 2) 
                        {
                            // Sekunden
                            longitudeRational  = longArr[2];
                            
                            longitude += longitudeRational.floatValue() / 60.0f / 60.0f;
                        }
                    }
                }
            }            
        }
        catch (MetadataException mex) 
        {
            Logger.getLogger(getClass()).warn(mex);
        }
        
        return(longitude);
    }

    public String getGpsLongitudeRef()
    {
        if (gpsDirectory == null)
        {
            return("");
        }
        
        return gpsDirectory.getString(GpsDirectory.TAG_GPS_LONGITUDE_REF);
    }
    
}
