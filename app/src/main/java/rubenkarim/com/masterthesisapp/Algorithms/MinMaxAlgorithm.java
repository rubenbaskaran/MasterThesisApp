package rubenkarim.com.masterthesisapp.Algorithms;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;

import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.Models.InterestPointModel;
import rubenkarim.com.masterthesisapp.Models.RoiModel;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;
import rubenkarim.com.masterthesisapp.Utilities.Scaling;

public class MinMaxAlgorithm extends AbstractAlgorithm {

    private String imagePath;
    private RoiModel leftEye;
    private RoiModel rightEye;
    private RoiModel nose;
    private int[] center;
    private int radius;
    private Bitmap capturedImageBitmap;
    private Bitmap modifiedBitmap;

    public MinMaxAlgorithm(String imagePath, RoiModel leftEye, RoiModel rightEye, RoiModel nose, RoiModel cameraPreviewElement) {
        this.imagePath = imagePath;
        ImageProcessing.FixImageOrientation(imagePath);
        capturedImageBitmap = BitmapFactory.decodeFile(imagePath);
        modifiedBitmap = capturedImageBitmap.copy(Bitmap.Config.ARGB_8888, true);

        int[] imageOriginalDimensions = new int[]{capturedImageBitmap.getWidth(), capturedImageBitmap.getHeight()};
        int[] cameraPreviewDimensions = new int[]{cameraPreviewElement.getWidth(), cameraPreviewElement.getHeight()};

        double scalingFactorX = (double) imageOriginalDimensions[0] / (double) cameraPreviewDimensions[0];
        double scalingFactorY = (double) imageOriginalDimensions[1] / (double) cameraPreviewDimensions[1];
        int horizontalOffset = cameraPreviewElement.getUpperLeftCornerLocation()[1];

        this.leftEye = Scaling.GetScaledRoiObject(leftEye, scalingFactorX, scalingFactorY, horizontalOffset);
        this.rightEye = Scaling.GetScaledRoiObject(rightEye, scalingFactorX, scalingFactorY, horizontalOffset);
        this.nose = Scaling.GetScaledRoiObject(nose, scalingFactorX, scalingFactorY, horizontalOffset);
    }

    @Override
    public GradientModel getGradientAndPositions() {
        InterestPointModel leftEyeMax = GetMaxMinSpotInRoi(leftEye, "max");
        InterestPointModel rightEyeMax = GetMaxMinSpotInRoi(rightEye, "max");
        InterestPointModel noseMin = GetMaxMinSpotInRoi(nose, "min");

        SaveDuplicateImageForTestingPurpose();

        if (leftEyeMax.getValue() > rightEyeMax.getValue()) {
            return new GradientModel(leftEyeMax.getValue() - noseMin.getValue(), leftEyeMax.getPosition(), noseMin.getPosition());
        }
        else {
            return new GradientModel(rightEyeMax.getValue() - noseMin.getValue(), rightEyeMax.getPosition(), noseMin.getPosition());
        }
    }

    private InterestPointModel GetMaxMinSpotInRoi(RoiModel roiCircle, String category) {
        int width = roiCircle.getWidth();
        int height = roiCircle.getHeight();
        radius = roiCircle.getWidth() / 2;
        int[] leftUpperCornerLocation = roiCircle.getUpperLeftCornerLocation();
        center = new int[]{leftUpperCornerLocation[0] + radius, leftUpperCornerLocation[1] + radius};
        int totalCounter = 0;
        int counter = 0;
        int[] position = new int[2];
        double maxValue = 0;
        double minValue = 765;

        for (int x = leftUpperCornerLocation[0]; x <= leftUpperCornerLocation[0] + width; x++) {
            for (int y = leftUpperCornerLocation[1]; y <= leftUpperCornerLocation[1] + height; y++) {
                totalCounter += 1;
                if (isPixelInsideRoi(x, y)) {
                    counter += 1;
                    int targetPixel = capturedImageBitmap.getPixel(x, y);
                    Log.e("Target pixel", "x: " + x + ", y: " + y);
                    Log.e("Pixel color", Color.red(targetPixel) + "," + Color.green(targetPixel) + "," + Color.blue(targetPixel));
                    double colorSum = Color.red(targetPixel) + Color.green(targetPixel) + Color.blue(targetPixel);

                    if (category.equals("max")) {
                        maxValue = Math.max(colorSum, maxValue);
                        position = new int[]{x, y};
                    }
                    else {
                        minValue = Math.min(colorSum, minValue);
                        position = new int[]{x, y};
                    }

                    modifiedBitmap.setPixel(x, y, Color.YELLOW);
                }
            }
        }

        Log.e("totalCounter", String.valueOf(totalCounter));
        Log.e("Counter", String.valueOf(counter));

        return new InterestPointModel(category.equals("max") ? maxValue : minValue, position);
    }

    private boolean isPixelInsideRoi(int pixelX, int pixelY) {
        double distanceFromCenterToPixel = Math.sqrt(Math.pow(center[0] - pixelX, 2) + Math.pow(center[1] - pixelY, 2));
        return distanceFromCenterToPixel <= radius;
    }

    private void SaveDuplicateImageForTestingPurpose() {
        String[] splittedImagePath = imagePath.split("\\.");
        String duplicateImagePath = splittedImagePath[0] + "_bitmap." + splittedImagePath[1];
        try (FileOutputStream out = new FileOutputStream(duplicateImagePath)) {
            modifiedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
            Log.e("MinMaxAlgorithm", "Saved image with yellow dot");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}