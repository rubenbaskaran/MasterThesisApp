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

    public static int[] getScaledMarkerPosition(int[] markerPosition, int[] imageOriginalDimensions, int[] imageViewDimensions, int horizontalOffset) {
        double scalingFactorX = (double)imageOriginalDimensions[0] / (double)imageViewDimensions[0];
        double scalingFactorY = (double)imageOriginalDimensions[1] / (double)imageViewDimensions[1];
        double positionX = (double)markerPosition[0] / scalingFactorX;
        double positionY = (double)markerPosition[1] / scalingFactorY;

        return new int[]{(int)positionX, (int)positionY};
    }
}
