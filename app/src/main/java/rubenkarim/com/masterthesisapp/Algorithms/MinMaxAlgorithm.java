package rubenkarim.com.masterthesisapp.Algorithms;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import rubenkarim.com.masterthesisapp.Managers.AlgorithmManager;
import rubenkarim.com.masterthesisapp.Models.RoiModel;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;

public class MinMaxAlgorithm extends AlgorithmManager {

    private String imagePath;
    private RoiModel leftEye;
    private RoiModel rightEye;
    private RoiModel nose;
    private int[] center;
    private int radius;

    public MinMaxAlgorithm(String imagePath, RoiModel leftEye, RoiModel rightEye, RoiModel nose) {
        this.imagePath = imagePath;
        this.leftEye = leftEye;
        this.rightEye = rightEye;
        this.nose = nose;
    }

    @Override
    public double calculateGradient() {

        // TODO: Convert default_picture in drawble to bitmap
        //getListOfRoiPixels(imageView_leftEye, container);

        return 0;
    }

    public void getListOfRoiPixels(ImageView roiCircle, View capturedImage) {
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

    private boolean isPixelInsideRoi(int pixelX, int pixelY) {
        double distanceFromCenterToPixel = Math.sqrt(Math.pow(center[0] - pixelX, 2) + Math.pow(center[1] - pixelY, 2));
        return distanceFromCenterToPixel <= radius;
    }
}