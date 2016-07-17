package android.webfilesys.de.webfilesysblog;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by fho on 17.07.2016.
 */
public class PictureUtils {

    private static final String ROTATED_TEMP_IMAGE_FILENAME = "webfilesysTempRotatedImage.bmp";

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public static Bitmap lowMemoryRotation(Bitmap bitmap, ContextWrapper activity) {
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        try {
            final DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(activity.openFileOutput(ROTATED_TEMP_IMAGE_FILENAME, Context.MODE_PRIVATE)));
            for (int x = 0; x < width; x++) {
                for (int y = height - 1; y >= 0; y--) {
                    final int pixel = bitmap.getPixel(x, y);
                    outputStream.writeInt(pixel);
                }
            }

            outputStream.flush();
            outputStream.close();

            bitmap.recycle();

            final int newWidth = height;
            final int newHeight = width;
            bitmap = Bitmap.createBitmap(newWidth, newHeight, bitmap.getConfig());
            final DataInputStream inputStream = new DataInputStream(new BufferedInputStream(activity.openFileInput(ROTATED_TEMP_IMAGE_FILENAME)));
            for (int y = 0; y < newHeight; y++) {
                for (int x = 0; x < newWidth; x++) {
                    final int pixel = inputStream.readInt();
                    bitmap.setPixel(x, y, pixel);
                }
            }
            inputStream.close();

            new File(activity.getFilesDir(), ROTATED_TEMP_IMAGE_FILENAME).delete();
            // saveBitmapToFile(bitmap); //for checking the output

            return bitmap;
        } catch (final IOException e) {
            Log.e("webfilesysblog", "low memory rotation failed", e);
        }

        return null;
    }
}
