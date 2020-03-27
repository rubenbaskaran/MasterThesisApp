package rubenkarim.com.masterthesisapp.Algorithms;

import android.content.Context;
import android.graphics.Bitmap;

import rubenkarim.com.masterthesisapp.Models.GradientModel;

// Sample – face-detection – is the simplest implementation of the face detection functionality on Android.
// It supports 2 modes of execution: available by default Java wrapper for the cascade classifier,
// and manually crafted JNI call to a native class which supports tracking.
// Even Java version is able to show close to the real-time performance on a Google Nexus One device.

// Checkout DetectionBasedTracker.java and FdActivity.java

public class RgbThermalAlgorithm extends AbstractAlgorithm {

    private Bitmap rgbImage;
    private Bitmap thermalImage;
    private Context context;

    public RgbThermalAlgorithm(Bitmap rgbImage, Bitmap thermalImage, Context context) {

        this.rgbImage = rgbImage;
        this.thermalImage = thermalImage;
        this.context = context;
    }

    private void findLandmarksOnRgbImage() {

    }

    private void mapLandmarksToThermalImage() {

    }

    @Override
    public GradientModel getGradientAndPositions() {
        return null;
    }
}
