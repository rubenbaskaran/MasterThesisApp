package rubenkarim.com.masterthesisapp.Algorithms;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.io.IOException;

import rubenkarim.com.masterthesisapp.Managers.AlgorithmManager;
import rubenkarim.com.masterthesisapp.Models.RoiModel;

public class MinMaxAlgorithm extends AlgorithmManager {

    private String imagePath;
    private RoiModel leftEye;
    private RoiModel rightEye;
    private RoiModel nose;
    private int[] center;
    private int radius;
    private Bitmap capturedImageBitmap;
    private Bitmap modifiedBitmap;

    public MinMaxAlgorithm(String imagePath, RoiModel leftEye, RoiModel rightEye, RoiModel nose) {
        this.imagePath = imagePath;
        this.leftEye = leftEye;
        this.rightEye = rightEye;
        this.nose = nose;
        capturedImageBitmap = BitmapFactory.decodeFile(imagePath);
        modifiedBitmap = capturedImageBitmap.copy( Bitmap.Config.ARGB_8888 , true);
    }

    @Override
    public double calculateGradient() {
        // TODO: What was the dimensions of the image when the template was applied
        // TODO: What is the dimension of the image when it is retrieved from memory and being processed

        getListOfRoiPixels(leftEye);

        return 0;
    }

    private void getListOfRoiPixels(RoiModel roiCircle) {
        int width = roiCircle.getWidth();
        int height = roiCircle.getHeight();
        radius = roiCircle.getWidth() / 2;
        int[] leftUpperCornerLocation = roiCircle.getUpperLeftCornerLocation();
        center = new int[]{leftUpperCornerLocation[0] + radius, leftUpperCornerLocation[1] + radius};
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
                    modifiedBitmap.setPixel(x, y, Color.YELLOW);
                }
            }
        }

        Log.e("totalCounter", String.valueOf(totalCounter));
        Log.e("Counter", String.valueOf(counter));
        saveImage();
    }

    private boolean isPixelInsideRoi(int pixelX, int pixelY) {
        double distanceFromCenterToPixel = Math.sqrt(Math.pow(center[0] - pixelX, 2) + Math.pow(center[1] - pixelY, 2));
        return distanceFromCenterToPixel <= radius;
    }

    private void saveImage() {
        try (FileOutputStream out = new FileOutputStream("/storage/emulated/0/Pictures/Masterthesisimages/14:14:25_bitmap.jpg")) {
            modifiedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            Log.e("MinMaxAlgorithm", "Saved image with yellow dot");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}