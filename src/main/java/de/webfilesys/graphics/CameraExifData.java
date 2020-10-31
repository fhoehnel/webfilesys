package de.webfilesys.graphics;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegSegmentMetadataReader;
import com.drew.imaging.jpeg.JpegSegmentType;
import com.drew.lang.Rational;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.drew.metadata.exif.GpsDirectory;

public class CameraExifData {
	public static final int ORIENTATION_UNKNOWN = (-1);
	public static final int ORIENTATION_LANDSCAPE = 1;
	public static final int ORIENTATION_PORTRAIT = 2;

	Metadata metadata = null;

	Directory exifDirectory = null;

	Directory gpsDirectory = null;
	
	Directory thumbnailDirectory = null;
	
	ExifIFD0Directory ifd0Directory = null;
	
	private int thumbHeight = 0;
	private int thumbWidth = 0;
	
	public static int TAG_THUMBNAIL_DATA = 0x10000;
	
	// reading the thumbnail data has been removed from the original metadata-extractor library to save memory
	// https://github.com/drewnoakes/metadata-extractor/issues/276
	static {
		List<JpegSegmentMetadataReader> allReaders = (List<JpegSegmentMetadataReader>) JpegMetadataReader.ALL_READERS;
		for (int n = 0, cnt = allReaders.size(); n < cnt; n++) {
			if (allReaders.get(n).getClass() != ExifReader.class) {
				continue;
			}
			
			allReaders.set(n, new ExifReader() {
				@Override
				public void readJpegSegments(@NotNull final Iterable<byte[]> segments, @NotNull final Metadata metadata, @NotNull final JpegSegmentType segmentType) {
					super.readJpegSegments(segments, metadata, segmentType);

				    for (byte[] segmentBytes : segments) {
				        // Filter any segments containing unexpected preambles
				        // if (!startsWithJpegExifPreamble(segmentBytes)) {
				        if (!startsWithJpegExifPreamble2(segmentBytes)) {
				        	continue;
				        }
				        
						// Extract the thumbnail
				        try {
				            ExifThumbnailDirectory tnDirectory = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);

				            if (tnDirectory != null && tnDirectory.containsTag(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET)) {
				            	int offset = tnDirectory.getInt(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET);
				            	int length = tnDirectory.getInt(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH);
				            	byte[] tnData = new byte[length];
				            	System.arraycopy(segmentBytes, JPEG_SEGMENT_PREAMBLE.length() + offset, tnData, 0, length);
				            	tnDirectory.setObject(TAG_THUMBNAIL_DATA, tnData);
				            }
				        } catch (MetadataException e) {
							Logger.getLogger(getClass()).error("failed to read thumbnail data", e);
				        }
				    }
				}	
				
			    private boolean startsWithJpegExifPreamble2(byte[] bytes) {
			        return bytes.length >= JPEG_SEGMENT_PREAMBLE.length() &&
			            new String(bytes, 0, JPEG_SEGMENT_PREAMBLE.length()).equals(JPEG_SEGMENT_PREAMBLE);
			    }
				
			});
			break;
		}
	}	
	
	public CameraExifData(String imgFileName) {
		File jpegFile = new File(imgFileName);

		try {
			Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);
			exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
			gpsDirectory = metadata.getFirstDirectoryOfType(GpsDirectory.class);
			thumbnailDirectory = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
			ifd0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
		} catch (Exception ex) {
			Logger.getLogger(getClass()).warn("failed to extract exif data from file " + imgFileName + ": " + ex);
		}
	}

	public boolean hasExifData() {
		if (exifDirectory == null) {
			return false;
		}

		return (exifDirectory.getTagCount() > 0);
	}

	public void printExifData() {
		if (exifDirectory == null) {
			System.out.println("exif directory is null");
			return;
		}

		try {
			for (Tag tag : exifDirectory.getTags()) {

				System.out.print(tag.getTagType() + " , ");
				System.out.print(tag.getTagName() + " , ");

				try {
					System.out.println(tag.getDescription());
				} catch (java.lang.NoSuchMethodError nsm) {
					System.out.println(nsm);
				}
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	public String getManufacturer() {
		if (ifd0Directory == null) {
			return ("");
		}

		return (ifd0Directory.getString(ExifSubIFDDirectory.TAG_MAKE));
	}

	public String getCameraModel() {
		if (ifd0Directory == null) {
			return ("");
		}

		return (ifd0Directory.getString(ExifSubIFDDirectory.TAG_MODEL));
	}

	public String getExposureTime() {
		if (exifDirectory == null) {
			return (null);
		}

		return (exifDirectory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
	}

	public String getAperture() {
		if (exifDirectory == null) {
			return ("");
		}

		return (exifDirectory.getString(ExifSubIFDDirectory.TAG_FNUMBER));
	}

	public String getISOValue() {
		if (exifDirectory == null) {
			return ("");
		}

		return (exifDirectory.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
	}

	public String getFocalLength() {
		if (exifDirectory == null) {
			return ("");
		}

		return (exifDirectory.getString(ExifSubIFDDirectory.TAG_FOCAL_LENGTH));
	}

	public Date getExposureDate() {
		if (exifDirectory == null) {
			return (null);
		}

		return (exifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
	}

	/**
	 * 0x9209: ('Flash', {0: 'No', 1: 'Fired', 5: 'Fired (?)', # no return sensed 7:
	 * 'Fired (!)', # return sensed 9: 'Fill Fired', 13: 'Fill Fired (?)', 15: 'Fill
	 * Fired (!)', 16: 'Off', 24: 'Auto Off', 25: 'Auto Fired', 29: 'Auto Fired
	 * (?)', 31: 'Auto Fired (!)', 32: 'Not Available'}),
	 */
	public int getFlashFired() {
		if (exifDirectory == null) {
			return (-1);
		}

		try {
			return (exifDirectory.getInt(ExifSubIFDDirectory.TAG_FLASH));
		} catch (MetadataException metex) {
			return (-1);
		}
	}

	public double getExposureBias() {
		if (exifDirectory == null) {
			return (Double.MIN_VALUE);
		}

		try {
			return exifDirectory.getDouble(ExifSubIFDDirectory.TAG_EXPOSURE_BIAS);
		} catch (MetadataException metex) {
			return (-1);
		}
	}

	public int getImageWidth() {
		if (exifDirectory == null) {
			return (-1);
		}

		try {
			return (exifDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH));
		} catch (MetadataException metex) {
			return (-1);
		}
	}

	public int getImageHeigth() {
		if (exifDirectory == null) {
			return (-1);
		}

		try {
			return (exifDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT));
		} catch (MetadataException metex) {
			return (-1);
		}
	}

	public int getThumbnailOffset() {
		if (exifDirectory == null) {
			return (-1);
		}

		try {
			return (exifDirectory.getInt(ExifThumbnailDirectory.TAG_THUMBNAIL_OFFSET));
		} catch (MetadataException metex) {
			return (-1);
		}
	}

	public int getThumbnailLength() {
		if (thumbnailDirectory == null) {
			return (-1);
		}
		
		if (!thumbnailDirectory.containsTag(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH)) {
			return (-1);
		}

		try {
			return (thumbnailDirectory.getInt(ExifThumbnailDirectory.TAG_THUMBNAIL_LENGTH));
		} catch (MetadataException metex) {
			return (-1);
		}
	}

	public int getThumbnailWidth() {
		if (thumbnailDirectory == null) {
			return (-1);
		}

		if (!thumbnailDirectory.containsTag(ExifThumbnailDirectory.TAG_IMAGE_WIDTH)) {
			return (-1);
		}

		try {
			return (thumbnailDirectory.getInt(ExifThumbnailDirectory.TAG_IMAGE_WIDTH));
		} catch (MetadataException metex) {
			return (-1);
		}
	}

	public int getThumbnailHeight() {
		if (thumbnailDirectory == null) {
			return (-1);
		}

		if (!thumbnailDirectory.containsTag(ExifThumbnailDirectory.TAG_IMAGE_HEIGHT)) {
			return (-1);
		}

		try {
			return (thumbnailDirectory.getInt(ExifThumbnailDirectory.TAG_IMAGE_HEIGHT));
		} catch (MetadataException metex) {
			return (-1);
		}
	}

	public int getOrientation() {
		if (ifd0Directory == null) {
			return (ORIENTATION_UNKNOWN);
		}

		if (!ifd0Directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
			return (ORIENTATION_UNKNOWN);
		}

		try {
			return ifd0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
		} catch (MetadataException metex) {
			return (ORIENTATION_UNKNOWN);
		}
	}

	public int getThumbnailOrientation() {
		if (thumbnailDirectory == null) {
			return (ORIENTATION_UNKNOWN);
		}

		if (!thumbnailDirectory.containsTag(ExifThumbnailDirectory.TAG_ORIENTATION)) {
			return (ORIENTATION_UNKNOWN);
		}

		try {
			return thumbnailDirectory.getInt(ExifThumbnailDirectory.TAG_ORIENTATION);
		} catch (MetadataException metex) {
			return (ORIENTATION_UNKNOWN);
		}
	}
	
	public byte[] getThumbnailData() {
		if (thumbnailDirectory == null) {
			return (null);
		}
		try {
			return (thumbnailDirectory.getByteArray(TAG_THUMBNAIL_DATA));
		} catch (Exception metex) {
			return (null);
		}
	}

	/**
	 * Because the TAG_THUMBNAIL_IMAGE_HEIGHT and TAG_THUMBNAIL_IMAGE_WIDTH are not
	 * reliable, the thumbnail dimensions are determined here from the thumnail
	 * image data
	 */
	public void getThumbnailDimensions() {
		if (thumbnailDirectory == null) {
			return;
		}
		if (!thumbnailDirectory.containsTag(TAG_THUMBNAIL_DATA)) {
			Logger.getLogger(getClass()).debug("missing EXIF thumbnail data tag");
			return;
		}

		byte thumbData[] = null;

		try {
			thumbData = thumbnailDirectory.getByteArray(TAG_THUMBNAIL_DATA);
		} catch (Exception metaEx) {
			Logger.getLogger(getClass()).warn(metaEx);
			return;
		}
		
		byte marker1 = (byte) 0xff;
		byte marker2 = (byte) 0xc2;
		byte marker3 = (byte) 0xc0;

		for (int i = 0; (i < thumbData.length - 9); i++) {
			if (thumbData[i] == marker1) {
				if ((thumbData[i + 1] == marker2) || (thumbData[i + 1] == marker3)) {
					if ((thumbData[i + 2] == 0) && (thumbData[i + 3] == 17) && (thumbData[i + 4] == 8)) {
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
	 * Determine the orientation from the thumbnail image data because the EXIF
	 * orientaion tag is not reliable
	 * 
	 * @return lanscape=1, portrait=2, unknown=0
	 */
	public int getThumbOrientation() {
		if ((thumbHeight == 0) || (thumbWidth == 0)) {
			getThumbnailDimensions();
		}

		if ((thumbHeight == 0) || (thumbWidth == 0)) {
			return (ORIENTATION_UNKNOWN);
		}

		if (thumbHeight > thumbWidth) {
			return (ORIENTATION_PORTRAIT);
		}

		return (ORIENTATION_LANDSCAPE);
	}

	public int getThumbWidth() {
		if (thumbWidth == 0) {
			getThumbnailDimensions();
		}

		return (thumbWidth);
	}

	public int getThumbHeight() {
		if (thumbHeight == 0) {
			getThumbnailDimensions();
		}

		return (thumbHeight);
	}

	public float getGpsLatitude() {
		if (gpsDirectory == null) {
			return (-1.0f);
		}

		float latitude = (-1.0f);

		try {
			if (gpsDirectory.containsTag(GpsDirectory.TAG_LATITUDE)) {
				Rational[] latArr = gpsDirectory.getRationalArray(GpsDirectory.TAG_LATITUDE);

				if ((latArr != null) && (latArr.length > 0)) {
					// Grad
					Rational latitudeRational = latArr[0];

					latitude = latitudeRational.floatValue();

					if (latArr.length > 1) {
						// Minuten
						latitudeRational = latArr[1];

						latitude += latitudeRational.floatValue() / 60.0f;

						if (latArr.length > 2) {
							// Sekunden
							latitudeRational = latArr[2];

							latitude += latitudeRational.floatValue() / 60.0f / 60.0f;
						}
					}
				}
			}
		} catch (Exception mex) {
			Logger.getLogger(getClass()).warn(mex);
		}

		return (latitude);
	}

	public String getGpsLatitudeRef() {
		if (gpsDirectory == null) {
			return ("");
		}

		return gpsDirectory.getString(GpsDirectory.TAG_LATITUDE_REF);
	}

	public float getGpsLongitude() {
		if (gpsDirectory == null) {
			return (-1.0f);
		}

		float longitude = (-1.0f);

		try {
			if (gpsDirectory.containsTag(GpsDirectory.TAG_LONGITUDE)) {
				Rational[] longArr = gpsDirectory.getRationalArray(GpsDirectory.TAG_LONGITUDE);

				if ((longArr != null) && (longArr.length > 0)) {
					Rational longitudeRational = longArr[0];

					longitude = longitudeRational.floatValue();

					if (longArr.length > 1) {
						// Minuten
						longitudeRational = longArr[1];

						longitude += longitudeRational.floatValue() / 60.0f;

						if (longArr.length > 2) {
							// Sekunden
							longitudeRational = longArr[2];

							longitude += longitudeRational.floatValue() / 60.0f / 60.0f;
						}
					}
				}
			}
		} catch (Exception mex) {
			Logger.getLogger(getClass()).warn(mex);
		}

		return (longitude);
	}

	public String getGpsLongitudeRef() {
		if (gpsDirectory == null) {
			return ("");
		}

		return gpsDirectory.getString(GpsDirectory.TAG_LONGITUDE_REF);
	}

}
