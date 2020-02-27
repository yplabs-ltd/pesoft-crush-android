package net.pesofts.crush.Util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class ImageUtil {

    public static String getFilePathFromUri(Uri uri, Context context) {
        if (uri == null) {
            return null;
        }

        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
            cursor.moveToFirst();
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();

            return path;
        } catch (Exception e) {
            if (uri.toString().startsWith("file://")) {
                return uri.toString().substring("file://".length());
            }

            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static File createTempImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        String directoryName = "pesoft_crush";

        final File storageDir = new File(Environment.getExternalStorageDirectory(), directoryName);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        return File.createTempFile(imageFileName, /* prefix */
                ".jpg"/* suffix */, storageDir /* directory */
        );
    }

    public static File createTempImageFileForProfile() throws IOException {
        String imageFileName = "JPEG_PROFILE";
        String directoryName = "pesoft_crush";

        final File storageDir = new File(Environment.getExternalStorageDirectory(), directoryName);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        return new File(storageDir, imageFileName);
    }

    public static File getFileFromUri(Uri uri, Context context) {
        String filePath = getFilePathFromUri(uri, context);
        if (StringUtil.isEmpty(filePath)) {
            return null;
        }

        return new File(filePath);
    }

    public static Bitmap getResizedBitmapFromFile(File file, int targetWidth) {
        try {
            if (file == null || file.length() == 0) {
                return null;
            }

            if (isLowcostDevice()) {
                targetWidth /= 2;
            }

            int targetWidthHeight[] = getTargetWidthHeight(getImageFileWidthHeight(file), targetWidth);
            if (targetWidthHeight == null) {
                return null;
            }

            return decodeSampledBitmapFromFile(file, targetWidthHeight[0], targetWidthHeight[1]);
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap decodeSampledBitmapFromFile(File file, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = getBitmapOptions(file);

        if (options == null) {
            return null;
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        Matrix matrix = new Matrix();
        matrix.postRotate(getImageOrientationDegree(file.getAbsolutePath()));

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static int getImageOrientationDegree(String absoluteFilePath) {
        int degree;
        int orientation = -1;

        try {
            ExifInterface exifInterface = new ExifInterface(absoluteFilePath);
            orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            LogUtil.e(LogUtil.TAG, e.getMessage(), e);
        }

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
            default:
                degree = 0;
                break;
        }

        return degree;
    }

    private static boolean isLowcostDevice() {
        final long MEMORY_THRESHOLD = 32 * 1024 * 1024;
        final long DEVICE_MEMORY = Runtime.getRuntime().maxMemory();

        return (DEVICE_MEMORY <= MEMORY_THRESHOLD || Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD);
    }

    public static int[] getImageFileWidthHeight(File imageFile) {
        BitmapFactory.Options options = getBitmapOptions(imageFile);
        if (options == null) {
            return null;
        }

        int result[] = new int[2];

        result[0] = options.outWidth;
        result[1] = options.outHeight;

        return result;
    }

    public static BitmapFactory.Options getBitmapOptions(File file) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        return options;
    }

    private static int[] getTargetWidthHeight(int[] originalWidthHeight, int targetWidth) {
        float ratio = ((float) originalWidthHeight[1] / (float) originalWidthHeight[0]);
        int targetHeight = (int) (targetWidth * ratio);

        originalWidthHeight[0] = targetWidth;
        originalWidthHeight[1] = targetHeight;

        return originalWidthHeight;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void deleteImageByUri(Uri uri, Context context) {
        try {
            File file = getFileFromUri(uri, context);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception ignored) {
        }
    }

    public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.postRotate(degrees);
            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b;
    }

    public static File bitmapToFile(Bitmap bitmap, File imageFile) {
        FileOutputStream outputStream = null;
        try {
//            File imageFile = new File(path);
            outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                    outputStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
