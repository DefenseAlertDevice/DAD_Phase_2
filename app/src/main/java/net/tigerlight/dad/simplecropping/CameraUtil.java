package net.tigerlight.dad.simplecropping;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import net.tigerlight.dad.registration.util.Constant;
import net.tigerlight.dad.util.Preference;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraUtil {
    // directory name to store captured images
    private static final String IMAGE_DIRECTORY_NAME = "DadApp";
    private static final String IMAGE_DIRECTORY_NAME_EMAIL = "DadAppProfileWithEmail";
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final String IMG_PREFIX = "IMG_";
    private static final String IMG_POSTFIX = ".jpg";

    /**
     * Creating file uri to store image/video
     */
    public static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image
     */
    public static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + IMG_PREFIX + timeStamp + IMG_POSTFIX);
        } else {
            return null;
        }

        return mediaFile;
    }

    public static Bitmap loadImageFromStorage(Context context, String numberKey, String path) {
        try {
            File file = new File(path, numberKey + ".jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);

            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

    }


    public static File getOutputMediaFileEmail(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME_EMAIL);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME_EMAIL, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME_EMAIL + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = Preference.getInstance().mSharedPreferences.getString(Constant.KEY_EMAIL, "");
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + IMG_POSTFIX);
        } else {
            return null;
        }

        return mediaFile;
    }


    /**
     * This method is use for check ExIfInof of image.
     *
     * @param mediaFile
     **/
    public static int checkExIfInfo(String mediaFile) {
        final ExifInterface exif;
        int rotation = 0;
        try {
            exif = new ExifInterface(mediaFile);
            final String exifOrientation = exif
                    .getAttribute(ExifInterface.TAG_ORIENTATION);
            if (exifOrientation.equals("6")) {
                rotation = 90;// Rotation angle
            } else if (exifOrientation.equals("1")) {
                rotation = 0;// Rotation angle
            } else if (exifOrientation.equals("8")) {
                rotation = 270;// Rotation angle
            } else if (exifOrientation.equals("3")) {
                rotation = 180;// Rotation angle
            } else if (exifOrientation.equals("0")) {
                rotation = 0;// Rotation angle
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotation;
    }

    /**
     * This method is use for rotation of image.
     *
     * @param mediaFile
     **/
    public static void rotateImage(String mediaFile, int rotation) {
        if (rotation != 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap bitmap = BitmapFactory.decodeFile(mediaFile, options);
            if (bitmap != null) {

                Matrix matrix = new Matrix();
                matrix.setRotate(rotation);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);
                // bitmap.recycle();
                try {
                    final FileOutputStream fos = new FileOutputStream(mediaFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    // bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    // bitmap.getWidth(), bitmap.getHeight(), rotateM, true);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

}
