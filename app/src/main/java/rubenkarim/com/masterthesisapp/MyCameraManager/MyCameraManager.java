package rubenkarim.com.masterthesisapp.MyCameraManager;

import android.content.Context;

import com.flir.thermalsdk.androidsdk.BuildConfig;
import com.flir.thermalsdk.androidsdk.ThermalSdkAndroid;
import com.flir.thermalsdk.image.ThermalImage;
import com.flir.thermalsdk.live.Camera;
import com.flir.thermalsdk.live.connectivity.ConnectionStatus;
import com.flir.thermalsdk.log.ThermalLog;

public class MyCameraManager {

    private static final String TAG = "CameraActivity";
    private Camera flirCamera;
    private ConnectionStatus connectionStatus;
    private Context cameraActivityContext;
    private boolean hasBeenInitialized = false;

    private static MyCameraManager instance;

    public static MyCameraManager getInstance() {
        if (instance == null) {
            instance = new MyCameraManager();
            return instance;
        } else {
            return instance;
        }
    }

    private void Initialized(Context applicationContext) {
        if(!hasBeenInitialized) {
            ThermalLog.LogLevel enableLoggingInDebug = BuildConfig.DEBUG ? ThermalLog.LogLevel.DEBUG : ThermalLog.LogLevel.NONE;
            ThermalSdkAndroid.init(applicationContext, enableLoggingInDebug);
            flirCamera = new Camera();
        }
    }

    private MyCameraManager() {
    }

    public void subscribeToFlirCamera(ThermalImagelistener thermalImagelistener){
        ThermalImage thermalImage = null;
        //TODO: Get FlirImage
        thermalImagelistener.subscribe(thermalImage);
    }

}
