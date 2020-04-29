package rubenkarim.com.masterthesisapp.Algorithms;

import com.flir.thermalsdk.image.ThermalImageFile;

import rubenkarim.com.masterthesisapp.Models.GradientModel;

public class TestAlgorthm extends AbstractAlgorithmTask {
    @Override
    public void getGradientAndPositions(AlgorithmResult algorithmResult) {

    }

    public GradientModel testCalculateGradient(int rigthEyeX, int rigthEyeY, int leftEyeX, int leftEyeY, int noseX, int noseY, ThermalImageFile thermalImageFile){
        return super.calculateGradient( rigthEyeX,  rigthEyeY,  leftEyeX,  leftEyeY,  noseX,  noseY,  thermalImageFile);
    }
}
