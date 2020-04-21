package rubenkarim.com.masterthesisapp.Algorithms;

import android.util.Log;

import com.flir.thermalsdk.image.ImageFactory;
import com.flir.thermalsdk.image.ThermalImageFile;

import junit.framework.TestFailure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import rubenkarim.com.masterthesisapp.Activities.MarkerActivity;
import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.Utilities.NeuralNetworkLoader;

class CnnAlgorithmTest {

    CnnAlgorithm mCnnAlgorithm;

    @BeforeEach
    void setUp() throws IOException {
        File thermalImg = new File("assets/Thermal_Test_img.jpg");
        System.out.println(thermalImg.getAbsolutePath());
        InputStream ip = new FileInputStream(thermalImg);
        ThermalImageFile thermalImageFile = (ThermalImageFile) ImageFactory.createImage(ip);
        File cnnFile = new File("./assets/RGB_yinguobingCNNV1.tflite");
        FileInputStream inputStream = new FileInputStream(cnnFile);
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = 0;
        long declaredLength = fileChannel.size();
        MappedByteBuffer m = fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
        mCnnAlgorithm = new CnnAlgorithm(m, thermalImageFile);
        System.out.println("done");
    }

    @Test
    void getGradientAndPositions() {
        mCnnAlgorithm.getGradientAndPositions(new AlgorithmResult() {
            @Override
            public void onResult(GradientModel gradientModel) {
                assertNotNull(gradientModel);
                assertNotNull(gradientModel.getEyePosition());
                assertNotNull(gradientModel.getNosePosition());
                assertNotEquals(0.0, gradientModel.getGradient());
                assertNotEquals(0.0, gradientModel.getEyeTemperature());
                assertNotEquals(0.0, gradientModel.getNoseTemperature());
            }

            @Override
            public void onError(String errorMessage) {
                assertNotNull(errorMessage);
            }
        });
    }


}