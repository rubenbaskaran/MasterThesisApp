package rubenkarim.com.masterthesisapp.Utilities;

import org.junit.jupiter.api.Test;

import rubenkarim.com.masterthesisapp.Models.RoiModel;

import static org.junit.jupiter.api.Assertions.*;

class ScalingTest {

    @Test
    void getScaledRoiObject() {
        //Testing scaling
        int[] upperleftcornerlocation = {1,2};
        RoiModel roiModel = Scaling.getScaledRoiObject(new RoiModel(upperleftcornerlocation,2, 2),1,1,0);
        assertArrayEquals(upperleftcornerlocation, roiModel.getUpperLeftCornerLocation(), "RoiModel should not be changed when scaling with one");

        upperleftcornerlocation = new int[] {2,2};
        roiModel = Scaling.getScaledRoiObject(new RoiModel(upperleftcornerlocation,2, 2),2,2,0);
        assertArrayEquals(new int[] {4,4}, roiModel.getUpperLeftCornerLocation(), "scaling is not calculated correct");

        //Testing Horizontal offset
        upperleftcornerlocation = new int[] {2,2};
        roiModel = Scaling.getScaledRoiObject(new RoiModel(upperleftcornerlocation,2, 2),1,1,1);
        assertArrayEquals(new int[] {2,1}, roiModel.getUpperLeftCornerLocation(), "Horizontal offset is not subtracted from y-axis");

        //Testing Horizontal offset and scaling
        upperleftcornerlocation = new int[] {2,2};
        roiModel = Scaling.getScaledRoiObject(new RoiModel(upperleftcornerlocation,2, 2),2,6,1);
        assertArrayEquals(new int[] {4,6}, roiModel.getUpperLeftCornerLocation(), "scaling is not calculated correct with horizontalOffset");
    }

    @Test
    void upscaleCoordinatesFromImageToScreen() {
    }

    @Test
    void downscaleCoordinatesFromScreenToImage() {
    }
}