package rubenkarim.com.masterthesisapp.Algorithms;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.flir.thermalsdk.image.ImageFactory;
import com.flir.thermalsdk.image.ThermalImageFile;

import java.io.IOException;

import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.Models.RoiModel;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;
import rubenkarim.com.masterthesisapp.Utilities.Logging;
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
    private String thermalImagePath;
    //endregion

    public MinMaxAlgorithm(String imagePath, RoiModel leftEye, RoiModel rightEye, RoiModel nose, RoiModel cameraPreviewElement) {
        try {
            thermalImagePath = imagePath;
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
        catch (Exception e) {
            //FIXME: Handle exception or pass it up
            Logging.error("MinMaxAlgorithm", e);
            throw e;
        }
    }

    @Override
    public GradientModel getGradientAndPositions() {
        try {
            int[] rightEyeMax = getMaxMinSpotInRoi(rightEye, "max");
            int[] leftEyeMax = getMaxMinSpotInRoi(leftEye, "max");
            int[] noseMin = getMaxMinSpotInRoi(nose, "min");

            return super.calculateGradient(
                    rightEyeMax[0],
                    rightEyeMax[1],
                    leftEyeMax[0],
                    leftEyeMax[1],
                    noseMin[0],
                    noseMin[1],
                    (ThermalImageFile) ImageFactory.createImage(thermalImagePath));
        }
        catch (IOException e) {
            //FIXME: Handle exception or pass it up
            Logging.error("getGradientAndPositions", e);
            return null;
        }
    }

    private int[] getMaxMinSpotInRoi(RoiModel roiCircle, String category) {
        try {
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

            return position;
        }
        catch (Exception e) {
            //FIXME: Handle exception or pass it up
            Logging.error("getMaxMinSpotInRoi", e);
            return null;
        }
    }

    private boolean isPixelInsideRoi(int pixelX, int pixelY) {
        try {
            return (Math.sqrt(Math.pow(center[0] - pixelX, 2) + Math.pow(center[1] - pixelY, 2))) < radius;
        }
        catch (Exception e) {
            //FIXME: Handle exception or pass it up
            Logging.error("isPixelInsideRoi", e);
            throw e;
        }
    }
}