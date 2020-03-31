package rubenkarim.com.masterthesisapp.Utilities;

import rubenkarim.com.masterthesisapp.Models.RoiModel;

public class Scaling {
    public static RoiModel getScaledRoiObject(RoiModel roi, double scalingFactorX, double scalingFactorY, int horizontalOffset) {
        double positionX = roi.getUpperLeftCornerLocation()[0] * scalingFactorX;
        double positionY = roi.getUpperLeftCornerLocation()[1] * scalingFactorY - (double) horizontalOffset * scalingFactorY;
        double width = roi.getWidth() * scalingFactorX;
        double height = roi.getHeight() * scalingFactorY;

        return new RoiModel(new int[]{(int) positionX, (int) positionY}, (int) width, (int) height);
    }

    public static int[] upscaleCoordinatesFromImageToScreen(int[] positionInCapturedImage, int[] capturedImageDimensions, int[] imageContainerDimensions) {
        double scalingFactorX = (double)imageContainerDimensions[0] / (double)capturedImageDimensions[0];
        double scalingFactorY = (double)imageContainerDimensions[1] / (double)capturedImageDimensions[1];
        double positionInImageContainerX = (double)positionInCapturedImage[0] * scalingFactorX;
        double positionInImageContainerY = (double)positionInCapturedImage[1] * scalingFactorY;

        return new int[]{(int)positionInImageContainerX, (int)positionInImageContainerY};
    }

    public static int[] downscaleCoordinatesFromScreenToImage(int[] positionInImageContainer, int[] capturedImageDimensions, int[] imageContainerDimensions) {
        double scalingFactorX = (double)capturedImageDimensions[0] / (double)imageContainerDimensions[0];
        double scalingFactorY = (double)capturedImageDimensions[1] / (double)imageContainerDimensions[1];
        double positionInCapturedImageX = (double)positionInImageContainer[0] * scalingFactorX;
        double positionInCapturedImageY = (double)positionInImageContainer[1] * scalingFactorY;

        return new int[]{(int)positionInCapturedImageX, (int)positionInCapturedImageY};
    }
}
