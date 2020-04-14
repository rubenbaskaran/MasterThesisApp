package rubenkarim.com.masterthesisapp.Utilities;

import org.junit.jupiter.api.Test;

import rubenkarim.com.masterthesisapp.Models.RoiModel;

import static org.junit.jupiter.api.Assertions.*;

class ScalingTest {

    @Test
    void getScaledRoiObject() {
        int[] upperleftcornerlocation = {1,2};
        RoiModel roiModel = Scaling.getScaledRoiObject(new RoiModel(upperleftcornerlocation,2, 2),1,1,0);
        assertArrayEquals(upperleftcornerlocation, roiModel.getUpperLeftCornerLocation(), "RoiModel should not be changed when scaling with one");

        upperleftcornerlocation = new int[] {2,2};
        RoiModel roiModel = Scaling.getScaledRoiObject(new RoiModel(upperleftcornerlocation,2, 2),1,1,0);

    }

    @Test
    void upscaleCoordinatesFromImageToScreen() {
    }

    @Test
    void downscaleCoordinatesFromScreenToImage() {
    }
}