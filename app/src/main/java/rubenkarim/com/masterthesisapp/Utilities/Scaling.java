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
        int capturedImageWidth = capturedImageDimensions[0];
        int capturedImageHeight = capturedImageDimensions[1];
        int imageContainerWidth = imageContainerDimensions[0];
        int imageContainerHeight = imageContainerDimensions[1];

        double scalingFactorX = (double)imageContainerWidth / (double)capturedImageWidth;
        double scalingFactorY = (double)imageContainerHeight / (double)capturedImageHeight;
        double positionInImageContainerX = (double)positionInCapturedImage[0] * scalingFactorX;
        double positionInImageContainerY = (double)positionInCapturedImage[1] * scalingFactorY;

        // Check whether coordinates are inside screen boundaries
        positionInImageContainerX = Math.max(positionInImageContainerX, 0);
        positionInImageContainerY = Math.max(positionInImageContainerY, 0);
        positionInImageContainerX = Math.min(positionInImageContainerX, imageContainerWidth-1);
        positionInImageContainerY = Math.min(positionInImageContainerY, imageContainerHeight-1);

        return new int[]{(int)positionInImageContainerX, (int)positionInImageContainerY};
    }

    public static int[] downscaleCoordinatesFromScreenToImage(int[] positionInImageContainer, int[] capturedImageDimensions, int[] imageContainerDimensions) {
        int capturedImageWidth = capturedImageDimensions[0];
        int capturedImageHeight = capturedImageDimensions[1];
        int imageContainerWidth = imageContainerDimensions[0];
        int imageContainerHeight = imageContainerDimensions[1];

        double scalingFactorX = (double) imageContainerWidth / (double) capturedImageWidth;
        double scalingFactorY = (double) imageContainerHeight / (double) capturedImageHeight;
        double positionInCapturedImageX = (double)positionInImageContainer[0] / scalingFactorX;
        double positionInCapturedImageY = (double)positionInImageContainer[1] / scalingFactorY;

        // Check whether coordinates are inside image boundaries
        positionInCapturedImageX = Math.max(positionInCapturedImageX, 0);
        positionInCapturedImageY = Math.max(positionInCapturedImageY, 0);
        positionInCapturedImageX = Math.min(positionInCapturedImageX, capturedImageWidth-1);
        positionInCapturedImageY = Math.min(positionInCapturedImageY, capturedImageHeight-1);

        return new int[]{(int)positionInCapturedImageX, (int)positionInCapturedImageY};
    }
}
