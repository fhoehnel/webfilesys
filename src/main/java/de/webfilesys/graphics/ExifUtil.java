package de.webfilesys.graphics;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.List;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants;
import org.apache.commons.imaging.formats.tiff.fieldtypes.FieldType;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputField;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

/**
 * Code based on code from bricolsoft consulting 
 * https://bricolsoftconsulting.com/copying-exif-metadata-using-sanselan/
 * @author bricolsoft consulting (modified)
 */
public class ExifUtil {
    public static final int TAG_ORIENTATION = 0x0112;
	
	public static boolean copyExifData(File sourceFile, File destFile, List<TagInfo> excludedFields) {
		
		String tempFileName = destFile.getAbsolutePath() + ".tmp";
		File tempFile = null;
		OutputStream tempStream = null;

		try {
			tempFile = new File(tempFileName);

			TiffOutputSet sourceSet = getSanselanOutputSet(sourceFile, TiffConstants.DEFAULT_TIFF_BYTE_ORDER);
			TiffOutputSet destSet = getSanselanOutputSet(destFile, sourceSet.byteOrder);

			// If the EXIF data endianess of the source and destination files
			// differ then fail. This only happens if the source and
			// destination images were created on different devices. It's
			// technically possible to copy this data by changing the byte
			// order of the data, but handling this case is outside the scope
			// of this implementation
			if (sourceSet.byteOrder != destSet.byteOrder)
				return false;

			destSet.getOrCreateExifDirectory();

			// Go through the source directories
			List<?> sourceDirectories = sourceSet.getDirectories();
			for (int i = 0; i < sourceDirectories.size(); i++) {
				TiffOutputDirectory sourceDirectory = (TiffOutputDirectory) sourceDirectories.get(i);
				TiffOutputDirectory destinationDirectory = getOrCreateExifDirectory(destSet, sourceDirectory);

				if (destinationDirectory == null)
					continue; // failed to create

				// Loop the fields
				List<?> sourceFields = sourceDirectory.getFields();
				for (int j = 0; j < sourceFields.size(); j++) {
					// Get the source field
					TiffOutputField sourceField = (TiffOutputField) sourceFields.get(j);

					// Check exclusion list
					if (excludedFields != null && excludedFields.contains(sourceField.tagInfo)) {
						destinationDirectory.removeField(sourceField.tagInfo);
						continue;
					}

					// Remove any existing field
					destinationDirectory.removeField(sourceField.tagInfo);

					// Add field
					destinationDirectory.add(sourceField);
				}
			}

			// Save data to destination
			tempStream = new BufferedOutputStream(new FileOutputStream(tempFile));
			new ExifRewriter().updateExifMetadataLossless(destFile, tempStream, destSet);
			tempStream.close();

			// Replace file
			if (destFile.delete()) {
				tempFile.renameTo(destFile);
			}

			return true;
		} catch (ImageReadException exception) {
			exception.printStackTrace();
		} catch (ImageWriteException exception) {
			exception.printStackTrace();
		} catch (IOException exception) {
			exception.printStackTrace();
		} finally {
			if (tempStream != null) {
				try {
					tempStream.close();
				} catch (IOException e) {
				}
			}

			if (tempFile != null) {
				if (tempFile.exists())
					tempFile.delete();
			}
		}

		return false;
	}

	public static boolean resetExifOrientation(File imgFile) {
		
		String tempFileName = imgFile.getAbsolutePath() + ".tmp";
		File tempFile = null;
		OutputStream tempStream = null;

		try {
			tempFile = new File(tempFileName);

			TiffOutputSet tiffOutputSet = getSanselanOutputSet(imgFile, TiffConstants.DEFAULT_TIFF_BYTE_ORDER);

			for (TiffOutputDirectory directory : tiffOutputSet.getDirectories()) {
				
				for (TiffOutputField field : directory.getFields()) {

					if (field.tagInfo.tag == TAG_ORIENTATION) {
					
				        byte[] orientationValue = new byte[1];
				        orientationValue[0] = 1;
				        
				        TiffOutputField orientationField =  new TiffOutputField(field.tagInfo, field.fieldType, field.count, orientationValue);		
				        
				        directory.removeField(TAG_ORIENTATION);
				        directory.add(orientationField);
					}
				}
			}

			tempStream = new BufferedOutputStream(new FileOutputStream(tempFile));
			new ExifRewriter().updateExifMetadataLossless(imgFile, tempStream, tiffOutputSet);
			tempStream.close();

			if (imgFile.delete()) {
				tempFile.renameTo(imgFile);
			}

			return true;
		} catch (ImageReadException exception) {
			exception.printStackTrace();
		} catch (ImageWriteException exception) {
			exception.printStackTrace();
		} catch (IOException exception) {
			exception.printStackTrace();
		} finally {
			if (tempStream != null) {
				try {
					tempStream.close();
				} catch (IOException e) {
				}
			}

			if (tempFile != null) {
				if (tempFile.exists())
					tempFile.delete();
			}
		}
		return false;
	}
	
	private static TiffOutputSet getSanselanOutputSet(File jpegImageFile, ByteOrder defaultByteOrder)
			throws IOException, ImageReadException, ImageWriteException {
		TiffImageMetadata exif = null;
		TiffOutputSet outputSet = null;

		ImageMetadata metadata = Imaging.getMetadata(jpegImageFile);
		JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
		if (jpegMetadata != null) {
			exif = jpegMetadata.getExif();

			if (exif != null) {
				outputSet = exif.getOutputSet();
			}
		}

		// If JPEG file contains no EXIF metadata, create an empty set
		// of EXIF metadata. Otherwise, use existing EXIF metadata to
		// keep all other existing tags
		if (outputSet == null)
			outputSet = new TiffOutputSet(exif == null ? defaultByteOrder : exif.contents.header.byteOrder);

		return outputSet;
	}

	private static TiffOutputDirectory getOrCreateExifDirectory(TiffOutputSet outputSet,
			TiffOutputDirectory outputDirectory) {
		TiffOutputDirectory result = outputSet.findDirectory(outputDirectory.type);
		if (result != null)
			return result;
		result = new TiffOutputDirectory(outputDirectory.type, TiffConstants.DEFAULT_TIFF_BYTE_ORDER);
		try {
			outputSet.addDirectory(result);
		} catch (ImageWriteException e) {
			return null;
		}
		return result;
	}
}
