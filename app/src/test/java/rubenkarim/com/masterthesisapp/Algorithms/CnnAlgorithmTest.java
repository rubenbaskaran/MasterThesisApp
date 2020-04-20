package rubenkarim.com.masterthesisapp.Algorithms;

import android.util.Log;

import com.flir.thermalsdk.image.ImageFactory;
import com.flir.thermalsdk.image.ThermalImageFile;

import junit.framework.TestFailure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

import rubenkarim.com.masterthesisapp.Activities.MarkerActivity;
import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.Utilities.NeuralNetworkLoader;

class CnnAlgorithmTest {

    CnnAlgorithm cnnAlgorithm;

    @BeforeEach
    void setUp() throws IOException {
        File cnnPath = new File("./");
        Log.d("TAG", cnnPath.getPath());
        ThermalImageFile thermalImageFile = (ThermalImageFile) ImageFactory.createImage("");
        //cnnAlgorithm = new CnnAlgorithm(NeuralNetworkLoader.loadCnn(this), thermalImageFile);
    }

    @Test
    void getGradientAndPositions() {
        cnnAlgorithm.getGradientAndPositions(new AlgorithmResult() {
            @Override
            public void onResult(GradientModel gradientModel) {
                assertNotNull(gradientModel);
                assertNotNull(gradientModel.getEyePosition());
                assertNotNull(gradientModel.getNosePosition());
                assertNotEquals(0.0, gradientModel.getGradient());
                assertNotEquals(0.0,gradientModel.getEyeTemperature());
                assertNotEquals(0.0,gradientModel.getNoseTemperature());
            }

            @Override
            public void onError(String errorMessage) {
                assertNotNull(errorMessage);
            }
        });
    }


}