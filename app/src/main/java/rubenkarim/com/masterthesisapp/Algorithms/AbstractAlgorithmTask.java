package rubenkarim.com.masterthesisapp.Algorithms;

import android.graphics.Bitmap;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.measurements.MeasurementSpot;

import rubenkarim.com.masterthesisapp.Models.GradientModel;

public abstract class AbstractAlgorithmTask {

    public abstract void getGradientAndPositions(AlgorithmResultListener algorithmResultListener);

    protected GradientModel calculateGradient(float[] coordinates, ThermalImageFile thermalImg){
        return calculateGradient(Math.round(coordinates[0]), Math.round(coordinates[1]), Math.round(coordinates[2]),
                Math.round(coordinates[3]), Math.round(coordinates[4]), Math.round(coordinates[5]), thermalImg);
    }

    protected GradientModel calculateGradient(int rigthEyeX, int rigthEyeY, int leftEyeX, int leftEyeY, int noseX, int noseY, ThermalImageFile thermalImg){

        thermalImg.getMeasurements().addSpot(rigthEyeX, rigthEyeY);
        thermalImg.getMeasurements().addSpot(leftEyeX, leftEyeY);
        thermalImg.getMeasurements().addSpot(noseX,noseY);

        MeasurementSpot rigthEye = thermalImg.getMeasurements().getSpots().get(0);
        MeasurementSpot leftEye = thermalImg.getMeasurements().getSpots().get(1);
        MeasurementSpot nose = thermalImg.getMeasurements().getSpots().get(2);

        final double temperatureRightEye = rigthEye.getValue().asCelsius().value;
        final double temperatureLeftEye = leftEye.getValue().asCelsius().value;
        final double temperatureNose = nose.getValue().asCelsius().value;

        if( temperatureRightEye > temperatureLeftEye){
            return new GradientModel(Math.abs(temperatureRightEye - temperatureNose),
                    new int[]{rigthEye.getPosition().x, rigthEye.getPosition().y},
                    new int[]{nose.getPosition().x, nose.getPosition().y},
                    temperatureRightEye,
                    temperatureNose);
        } else {
            return new GradientModel(Math.abs(temperatureLeftEye - temperatureNose),
                    new int[]{leftEye.getPosition().x, leftEye.getPosition().y},
                    new int[]{nose.getPosition().x, nose.getPosition().y},
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
