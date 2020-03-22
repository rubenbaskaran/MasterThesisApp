package rubenkarim.com.masterthesisapp.Algorithms;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.Models.InterestPointModel;
import rubenkarim.com.masterthesisapp.Models.RoiModel;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;
import rubenkarim.com.masterthesisapp.Utilities.Scaling;

public class MinMaxAlgorithm extends AbstractAlgorithm {

    //region Properties
    private RoiModel leftEye;
    private RoiModel rightEye;
    private RoiModel nose;
    private int width;
    private int height;
    private int[] center;
    private int radius;
    private Bitmap capturedImageBitmap;
    //endregion

    public MinMaxAlgorithm(String imagePath, RoiModel leftEye, RoiModel rightEye, RoiModel nose, RoiModel cameraPreviewElement) {
        ImageProcessing.FixImageOrientation(imagePath);
        capturedImageBitmap = ImageProcessing.convertToBitmap(imagePath);

        int[] imageOriginalDimensions = new int[]{capturedImageBitmap.getWidth(), capturedImageBitmap.getHeight()};
        int[] cameraPreviewDimensions = new int[]{cameraPreviewElement.getWidth(), cameraPreviewElement.getHeight()};

        double scalingFactorX = (double) imageOriginalDimensions[0] / (double) cameraPreviewDimensions[0];
        double scalingFactorY = (double) imageOriginalDimensions[1] / (double) cameraPreviewDimensions[1];
        int horizontalOffset = cameraPreviewElement.getUpperLeftCornerLocation()[1];

        this.leftEye = Scaling.getScaledRoiObject(leftEye, scalingFactorX, scalingFactorY, horizontalOffset);
        this.rightEye = Scaling.getScaledRoiObject(rightEye, scalingFactorX, scalingFactorY, horizontalOffset);
        this.nose = Scaling.getScaledRoiObject(nose, scalingFactorX, scalingFactorY, horizontalOffset);

        // All ROI circles have identical dimensions, hence arbitrary RoiModel is used
        width = this.leftEye.getWidth();
        height = this.leftEye.getHeight();
        radius = this.leftEye.getWidth() / 2;
    }

    @Override
    public GradientModel getGradientAndPositions() {
        InterestPointModel leftEyeMax = getMaxMinSpotInRoi(leftEye, "max");
        InterestPointModel rightEyeMax = getMaxMinSpotInRoi(rightEye, "max");
        InterestPointModel noseMin = getMaxMinSpotInRoi(nose, "min");

        if (leftEyeMax.getValue() > rightEyeMax.getValue()) {
            Log.e("MinMaxAlgorithm - getGradientAndPositions - returned", "left eye x: " + leftEyeMax.getPosition()[0] + ", left eye y: " + leftEyeMax.getPosition()[1]
                    + ". nose x: " + noseMin.getPosition()[0] + ", nose y: " + noseMin.getPosition()[1]
                    + ". original image x: " + capturedImageBitmap.getWidth() + ", original image y: " + capturedImageBitmap.getHeight());
            return new GradientModel(leftEyeMax.getValue() - noseMin.getValue(), leftEyeMax.getPosition(), noseMin.getPosition());
        }
        else {
            Log.e("MinMaxAlgorithm - getGradientAndPositions - returned", "right eye x: " + rightEyeMax.getPosition()[0] + ", right eye y: " + rightEyeMax.getPosition()[1]
                    + ". nose x: " + noseMin.getPosition()[0] + ", nose y: " + noseMin.getPosition()[1]
                    + ". original image x: " + capturedImageBitmap.getWidth() + ", original image y: " + capturedImageBitmap.getHeight());
            return new GradientModel(rightEyeMax.getValue() - noseMin.getValue(), rightEyeMax.getPosition(), noseMin.getPosition());
        }
    }

    private InterestPointModel getMaxMinSpotInRoi(RoiModel roiCircle, String category) {
        int[] leftUpperCornerLocation = roiCircle.getUpperLeftCornerLocation();
        center = new int[]{leftUpperCornerLocation[0] + radius, leftUpperCornerLocation[1] + radius};
        int[] position = new int[2];
        double maxValue = 0;
        double minValue = 765;
        double colorSum;
        int targetPixel;

        for (int x = leftUpperCornerLocation[0]; x <= leftUpperCornerLocation[0] + width; x++) {
            for (int y = leftUpperCornerLocation[1]; y <= leftUpperCornerLocation[1] + height; y++) {
                if (isPixelInsideRoi(x, y)) {
                    targetPixel = capturedImageBitmap.getPixel(x, y);
                    colorSum = Color.red(targetPixel) + Color.green(targetPixel) + Color.blue(targetPixel);

                    if (category.equals("max")) {
                        if (colorSum > maxValue) {
                            maxValue = colorSum;
                            position = new int[]{x, y};
                        }
                    }
                    else {
                        if (colorSum < minValue) {
                            minValue = colorSum;
                            position = new int[]{x, y};
                        }
                    }
                }
            }
        }

        return new InterestPointModel(category.equals("max") ? maxValue : minValue, position);
    }

    private boolean isPixelInsideRoi(int pixelX, int pixelY) {
        return (Math.sqrt(Math.pow(center[0] - pixelX, 2) + Math.pow(center[1] - pixelY, 2))) < radius;
    }
}