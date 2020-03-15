package rubenkarim.com.masterthesisapp.Utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.FileOutputStream;

public class ImageProcessing
{
    public static void FixImageOrientation(String filename)
    {
        int degree = checkImageOrientation(filename);
        if (degree != 0)
        {
            rotateImage(degree, filename);
        }
    }

    static int checkImageOrientation(String photoPath)
    {
        int degree = 0;

        try
        {
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation)
            {
                case ExifInterface.ORIENTATION_NORMAL:
                    degree = 0;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                case ExifInterface.ORIENTATION_UNDEFINED:
                    degree = 0;
                    break;
                default:
                    degree = 0;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return degree;
    }

    static String rotateImage(int degree, String imagePath)
    {
        if (degree <= 0)
        {
            return imagePath;
        }
        try
        {
            Bitmap b = BitmapFactory.decodeFile(imagePath);

            Matrix matrix = new Matrix();
            if (b.getWidth() > b.getHeight())
            {
                matrix.setRotate(degree);
                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
            }

            FileOutputStream fOut = new FileOutputStream(imagePath);
            String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            String imageType = imageName.substring(imageName.lastIndexOf(".") + 1);

            FileOutputStream out = new FileOutputStream(imagePath);
            if (imageType.equalsIgnoreCase("png"))
            {
                b.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            else if (imageType.equalsIgnoreCase("jpeg") || imageType.equalsIgnoreCase("jpg"))
            {
                b.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }
            fOut.flush();
            fOut.close();

            b.recycle();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return imagePath;
    }
}
