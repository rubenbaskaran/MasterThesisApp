package rubenkarim.com.masterthesisapp.Utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.view.View;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import rubenkarim.com.masterthesisapp.R;

/**
 * Source: https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera-intent-gets-rotated-on-some-devices-on-a
 */

public class ImageProcessing {
    public static void FixImageOrientation(String filename) throws IOException {
        int degree = checkImageOrientation(filename);
        if (degree != 0) {
            rotateImage(degree, filename);
        }
    }

    private static int checkImageOrientation(String photoPath) {
        int degree = 0;

        try {
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

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
                case ExifInterface.ORIENTATION_NORMAL:
                case ExifInterface.ORIENTATION_UNDEFINED:
                default:
                    degree = 0;
            }
        } catch (Exception e) {
            //FIXME: Handle exception or pass it up
            e.printStackTrace();
        }

        return degree;
    }

    private static void rotateImage(int degree, String imagePath) throws IOException {
        Bitmap b = ImageProcessing.convertToBitmap(imagePath);

        Matrix matrix = new Matrix();
        if (b.getWidth() > b.getHeight()) {
            matrix.setRotate(degree);
            b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
        }

        FileOutputStream fOut = new FileOutputStream(imagePath);
        String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        String imageType = imageName.substring(imageName.lastIndexOf(".") + 1);

        FileOutputStream out = new FileOutputStream(imagePath);
        if (imageType.equalsIgnoreCase("png")) {
            b.compress(Bitmap.CompressFormat.PNG, 100, out);
        } else if (imageType.equalsIgnoreCase("jpeg") || imageType.equalsIgnoreCase("jpg")) {
            b.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
        fOut.flush();
        fOut.close();

        b.recycle();
    }


    public static Bitmap loadBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    public static Bitmap convertToBitmap(String imagePath) throws NullPointerException {
        Bitmap btp;
        btp = BitmapFactory.decodeFile(imagePath);
        if(btp != null){
            return btp;
        } else {
            throw new NullPointerException("image cannot be found or converted imgPath: " + imagePath);
        }
    }

    public static FirebaseVisionImage convertToFirebaseVisionImage(Bitmap bitmap) {
        return FirebaseVisionImage.fromBitmap(bitmap);
    }

    public static Bitmap convertThermalImageFileToBitmap(ThermalImageFile thermalImageFile) {
        JavaImageBuffer javaBuffer = thermalImageFile.getImage();
        return BitmapAndroid.createBitmap(javaBuffer).getBitMap();
    }
}
