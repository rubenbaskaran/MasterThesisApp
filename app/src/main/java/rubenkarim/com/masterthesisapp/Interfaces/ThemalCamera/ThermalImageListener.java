package rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera;

import com.flir.thermalsdk.image.ThermalImage;

public interface ThermalImageListener {

    /**
     *Note is return on a non-UI tread as Flir standard...
     * use android method 'runOnUiThread(()->{ ...push image to ui inhere.. })'
     * @param thermalImage the image from the thermalCamera
     */
    void subscribe(ThermalImage thermalImage);
}
