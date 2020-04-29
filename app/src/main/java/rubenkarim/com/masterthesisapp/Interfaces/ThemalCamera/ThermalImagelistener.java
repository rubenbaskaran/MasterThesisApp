package rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera;

import com.flir.thermalsdk.image.ThermalImage;

public interface ThermalImagelistener {
    /**
     *Note is return on a non-UI tread as Flir standard...
     * use android method 'runOnUiThread(()->{ ...push image to ui inhere.. })'
     */
    void subscribe(ThermalImage thermalImage);
}
