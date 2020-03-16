package rubenkarim.com.masterthesisapp.Utilities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class RoiCalculator {
    static int[] center;
    static int radius;

    public static void getListOfRoiPixels(ImageView roiCircle, View capturedImage) {
        int width = roiCircle.getWidth();
        int height = roiCircle.getHeight();
        radius = roiCircle.getWidth() / 2;
        int[] leftUpperCornerLocation = new int[2];
        roiCircle.getLocationOnScreen(leftUpperCornerLocation);
        center = new int[]{leftUpperCornerLocation[0] + radius, leftUpperCornerLocation[1] + radius};
        Bitmap capturedImageBitmap = ImageProcessing.loadBitmapFromView(capturedImage);
        int totalCounter = 0;
        int counter = 0;

        for (int x = leftUpperCornerLocation[0]; x <= leftUpperCornerLocation[0] + width; x++) {
            for (int y = leftUpperCornerLocation[1]; y <= leftUpperCornerLocation[1] + height; y++) {
                totalCounter += 1;
                if (isPixelInsideRoi(x, y)) {
                    counter += 1;
                    int targetPixel = capturedImageBitmap.getPixel(x, y);
                    Log.e("Target pixel", "x: " + x + ", y: " + y);
                    Log.e("Pixel color", Color.red(targetPixel) + "," + Color.green(targetPixel) + "," + Color.blue(targetPixel));
                }
            }
        }

        Log.e("totalCounter", String.valueOf(totalCounter));
        Log.e("Counter", String.valueOf(counter));
    }

    private static boolean isPixelInsideRoi(int pixelX, int pixelY) {
        double distanceFromCenterToPixel = Math.sqrt(Math.pow(center[0] - pixelX, 2) + Math.pow(center[1] - pixelY, 2));
        return distanceFromCenterToPixel <= radius;
    }
}
