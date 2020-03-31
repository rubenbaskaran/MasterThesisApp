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

        // Check whether coordinates are inside image boundaries
        positionInCapturedImage[0] = Math.max(positionInCapturedImage[0], 10);
        positionInCapturedImage[1] = Math.max(positionInCapturedImage[1], 10);
        positionInCapturedImage[0] = Math.min(positionInCapturedImage[0], capturedImageWidth-10);
        positionInCapturedImage[1] = Math.min(positionInCapturedImage[1], capturedImageHeight-10);

        double scalingFactorX = (double)imageContainerWidth / (double)capturedImageWidth;
        double scalingFactorY = (double)imageContainerHeight / (double)capturedImageHeight;
        double positionInImageContainerX = (double)positionInCapturedImage[0] * scalingFactorX;
        double positionInImageContainerY = (double)positionInCapturedImage[1] * scalingFactorY;

        // Check whether coordinates are inside screen boundaries
        positionInImageContainerX = Math.max(positionInImageContainerX, 10);
        positionInImageContainerY = Math.max(positionInImageContainerY, 10);
        positionInImageContainerX = Math.min(positionInImageContainerX, imageContainerWidth-10);
        positionInImageContainerY = Math.min(positionInImageContainerY, imageContainerHeight-10);

        return new int[]{(int)positionInImageContainerX, (int)positionInImageContainerY};
    }

    public static int[] downscaleCoordinatesFromScreenToImage(int[] positionInImageContainer, int[] capturedImageDimensions, int[] imageContainerDimensions) {
        int capturedImageWidth = capturedImageDimensions[0];
        int capturedImageHeight = capturedImageDimensions[1];
        int imageContainerWidth = imageContainerDimensions[0];
        int imageContainerHeight = imageContainerDimensions[1];

        // Check whether coordinates are inside screen boundaries
        positionInImageContainer[0] = Math.max(positionInImageContainer[0], 10);
        positionInImageContainer[1] = Math.max(positionInImageContainer[1], 10);
        positionInImageContainer[0] = Math.min(positionInImageContainer[0], imageContainerWidth-10);
        positionInImageContainer[1] = Math.min(positionInImageContainer[1], imageContainerHeight-10);

        double scalingFactorX = (double) imageContainerWidth / (double) capturedImageWidth;
        double scalingFactorY = (double) imageContainerHeight / (double) capturedImageHeight;
        double positionInCapturedImageX = (double)positionInImageContainer[0] / scalingFactorX;
        double positionInCapturedImageY = (double)positionInImageContainer[1] / scalingFactorY;

        // Check whether coordinates are inside image boundaries
        positionInCapturedImageX = Math.max(positionInCapturedImageX, 10);
        positionInCapturedImageY = Math.max(positionInCapturedImageY, 10);
        positionInCapturedImageX = Math.min(positionInCapturedImageX, capturedImageWidth-10);
        positionInCapturedImageY = Math.min(positionInCapturedImageY, capturedImageHeight-10);

        return new int[]{(int)positionInCapturedImageX, (int)positionInCapturedImageY};
    }
}
