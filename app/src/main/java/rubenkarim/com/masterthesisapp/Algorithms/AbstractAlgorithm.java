package rubenkarim.com.masterthesisapp.Algorithms;

import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.ThermalValue;
import com.flir.thermalsdk.image.measurements.MeasurementSpot;

import rubenkarim.com.masterthesisapp.Models.GradientModel;

public abstract class AbstractAlgorithm {


    public abstract GradientModel getGradientAndPositions();

    private ThermalValue getGradient(Float[] coordinates, ThermalImageFile thermalImageFile){
        return getGradient(coordinates[0].intValue(), coordinates[1].intValue(), coordinates[2].intValue(),
                coordinates[3].intValue(),coordinates[4].intValue(), coordinates[5].intValue(), thermalImageFile);
    }

    private ThermalValue getGradient(int rigthEyeX, int rigthEyeY, int leftEyeX, int leftEyeY, int noseX, int noseY, ThermalImageFile thermalImageFile){

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
            return new ThermalValue((temperatureRightEye - temperatureNose), nose.getValue().unit);
        } else {
            return new ThermalValue((temperatureLeftEye - temperatureNose), nose.getValue().unit);
        }

    }
}
