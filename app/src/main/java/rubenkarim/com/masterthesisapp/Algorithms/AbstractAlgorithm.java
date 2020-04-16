package rubenkarim.com.masterthesisapp.Algorithms;

import android.graphics.Bitmap;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.ThermalValue;
import com.flir.thermalsdk.image.measurements.MeasurementSpot;

import rubenkarim.com.masterthesisapp.Models.GradientModel;

public abstract class AbstractAlgorithm {

    public abstract void getGradientAndPositions(AlgorithmResult algorithmResult);

    protected GradientModel calculateGradient(float[] coordinates, ThermalImageFile thermalImageFile){
        return calculateGradient(Math.round(coordinates[0]), Math.round(coordinates[1]), Math.round(coordinates[2]),
                Math.round(coordinates[3]), Math.round(coordinates[4]), Math.round(coordinates[5]), thermalImageFile);
    }

    protected GradientModel calculateGradient(int rigthEyeX, int rigthEyeY, int leftEyeX, int leftEyeY, int noseX, int noseY, ThermalImageFile thermalImageFile){

        thermalImageFile.getMeasurements().addSpot(rigthEyeX, rigthEyeY);
        thermalImageFile.getMeasurements().addSpot(leftEyeX, leftEyeY);
        thermalImageFile.getMeasurements().addSpot(noseX,noseY);

        MeasurementSpot rigthEye = thermalImageFile.getMeasurements().getSpots().get(0);
        MeasurementSpot leftEye = thermalImageFile.getMeasurements().getSpots().get(1);
        MeasurementSpot nose = thermalImageFile.getMeasurements().getSpots().get(2);

        final double temperatureRightEye = rigthEye.getValue().value;
        final double temperatureLeftEye = leftEye.getValue().value;
        final double temperatureNose = nose.getValue().value;

        if( temperatureRightEye > temperatureLeftEye){
            return new GradientModel((temperatureRightEye - temperatureNose),
                    new int[]{rigthEye.getPosition().x, rigthEye.getPosition().y},
                    new int[]{nose.getPosition().x, nose.getPosition().y},
                    rigthEye.getValue().value,
                    nose.getValue().value);
        } else {
            return new GradientModel((temperatureLeftEye - temperatureNose),
                    new int[]{leftEye.getPosition().x, leftEye.getPosition().y},
                    new int[]{nose.getPosition().x, nose.getPosition().y},
                    leftEye.getValue().value,
                    nose.getValue().value);
        }

    }

    protected Bitmap getBitmap(ThermalImageFile thermalImageFile) {
        JavaImageBuffer javaBuffer = thermalImageFile.getImage();
        return BitmapAndroid.createBitmap(javaBuffer).getBitMap();
    }
}
