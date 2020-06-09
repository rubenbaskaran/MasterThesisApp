package rubenkarim.com.masterthesisapp.Algorithms;

import android.graphics.Bitmap;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.measurements.MeasurementSpot;

import rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera.IThermalImage;
import rubenkarim.com.masterthesisapp.Models.GradientModel;

public abstract class AbstractAlgorithmTask {

    public abstract void getGradientAndPositions(AlgorithmResultListener algorithmResultListener);

    protected GradientModel calculateGradient(float[] coordinates, IThermalImage thermalImg){
        return calculateGradient(Math.round(coordinates[0]), Math.round(coordinates[1]), Math.round(coordinates[2]),
                Math.round(coordinates[3]), Math.round(coordinates[4]), Math.round(coordinates[5]), thermalImg);
    }

    protected GradientModel calculateGradient(int rigthEyeX, int rigthEyeY, int leftEyeX, int leftEyeY, int noseX, int noseY, IThermalImage thermalImg){

        final double temperatureRightEye = thermalImg.getTempertureAtPoint(rigthEyeX,rigthEyeY);
        final double temperatureLeftEye = thermalImg.getTempertureAtPoint(leftEyeX,leftEyeY);
        final double temperatureNose = thermalImg.getTempertureAtPoint(noseX,noseY);

        if( temperatureRightEye > temperatureLeftEye){
            return new GradientModel(Math.abs(temperatureRightEye - temperatureNose),
                    new int[]{rigthEyeX, rigthEyeY},
                    new int[]{noseX, noseX},
                    temperatureRightEye,
                    temperatureNose);
        } else {
            return new GradientModel(Math.abs(temperatureLeftEye - temperatureNose),
                    new int[]{leftEyeX, leftEyeY},
                    new int[]{noseX, noseX},
                    temperatureLeftEye,
                    temperatureNose);
        }

    }

    protected Bitmap getBitmap(ThermalImageFile thermalImageFile) {
        JavaImageBuffer javaBuffer = thermalImageFile.getImage();
        return BitmapAndroid.createBitmap(javaBuffer).getBitMap();
    }

    public static Double calculateTemperature(int x, int y, ThermalImageFile thermalImg){
        thermalImg.getMeasurements().clear();
        thermalImg.getMeasurements().addSpot(x, y);
        MeasurementSpot measurementSpot = thermalImg.getMeasurements().getSpots().get(0);
        return measurementSpot.getValue().asCelsius().value;
    }
}
