package rubenkarim.com.masterthesisapp.MyCameraManager;

import com.flir.thermalsdk.image.ThermalImage;

public interface ThermalImagelistener {
    /**
     *
     */
    void subscribe(ThermalImage thermalImage);
}
