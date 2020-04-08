package rubenkarim.com.masterthesisapp.Algorithms;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.flir.thermalsdk.image.ThermalImageFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import rubenkarim.com.masterthesisapp.Models.RoiModel;
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
    private ThermalImageFile capturedImageBitmap;
    private String mThermalImagePath;
    //endregion

    public MinMaxAlgorithm(ThermalImageFile thermalImage, RoiModel leftEye, RoiModel rightEye, RoiModel nose, RoiModel cameraPreviewElement) throws IOException {
        capturedImageBitmap = thermalImage;

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
    public void getGradientAndPositions(AlgorithmResult algorithmResult) {
        int[] rightEyeMax = getMaxMinSpotInRoi(rightEye, "max");
        int[] leftEyeMax = getMaxMinSpotInRoi(leftEye, "max");
        int[] noseMin = getMaxMinSpotInRoi(nose, "min");

        algorithmResult.onResult(super.calculateGradient(
                rightEyeMax[0],
                rightEyeMax[1],
                leftEyeMax[0],
                leftEyeMax[1],
                noseMin[0],
                noseMin[1],
                capturedImageBitmap));
    }

    private int[] getMaxMinSpotInRoi(RoiModel roiCircle, String category) {
        int[] leftUpperCornerLocation = roiCircle.getUpperLeftCornerLocation();
        center = new int[]{leftUpperCornerLocation[0] + radius, leftUpperCornerLocation[1] + radius};
        int[] position = new int[2];
        int groupSize = radius;
        double maxValue = 0;
        double minValue = 255 * 3 * (groupSize * groupSize);
        int targetPixel;
        Integer[] centerPointInPixelGroup = null;
        Bitmap bitmap = super.getBitmap(capturedImageBitmap);

        for (int x = leftUpperCornerLocation[0]; x < leftUpperCornerLocation[0] + width; x += groupSize) {
            for (int y = leftUpperCornerLocation[1]; y < leftUpperCornerLocation[1] + height; y += groupSize) {
                int colorSumInPixelGroup = 0;
                ArrayList groupOfPixels = new ArrayList();

                for (int nestedX = x; nestedX < x + groupSize; nestedX++) {
                    for (int nestedY = y; nestedY < y + groupSize; nestedY++) {
                        if (isPixelInsideRoi(nestedX, nestedY)) {
                            targetPixel = bitmap.getPixel(nestedX, nestedY);
                            int pixelColorSum = Color.red(targetPixel) + Color.green(targetPixel) + Color.blue(targetPixel);
                            colorSumInPixelGroup += pixelColorSum;
                            groupOfPixels.add(new Integer[]{pixelColorSum, nestedX, nestedY});

                            // Useful for illustrations in the report
                            // bitmap.setPixel(nestedX, nestedY, Color.RED);
                        }
                    }
                }

                Comparator<Integer[]> cmp = new Comparator<Integer[]>() {
                    @Override
                    public int compare(Integer[] integer, Integer[] t1) {
                        return integer[0].compareTo(t1[0]);
                    }
                };

                centerPointInPixelGroup = category.equals("max") ?
                        Collections.max(groupOfPixels, cmp) :
                        Collections.min(groupOfPixels, cmp);

                if (category.equals("max")) {
                    if (colorSumInPixelGroup > maxValue) {
                        maxValue = colorSumInPixelGroup;
                        position = new int[]{centerPointInPixelGroup[1], centerPointInPixelGroup[2]};
                    }
                }
                else {
                    if (colorSumInPixelGroup < minValue) {
                        minValue = colorSumInPixelGroup;
                        position = new int[]{centerPointInPixelGroup[1], centerPointInPixelGroup[2]};
                    }
                }
            }
        }

        return position;
    }

    private boolean isPixelInsideRoi(int pixelX, int pixelY) {

        return (Math.sqrt(Math.pow(center[0] - pixelX, 2) + Math.pow(center[1] - pixelY, 2))) < radius;
    }
}