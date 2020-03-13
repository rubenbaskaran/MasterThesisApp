package rubenkarim.com.masterthesisapp.MyCameraManager;

import android.content.Context;
import android.util.Log;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.BuildConfig;
import com.flir.thermalsdk.androidsdk.ThermalSdkAndroid;
import com.flir.thermalsdk.image.ThermalImage;
import com.flir.thermalsdk.live.Camera;
import com.flir.thermalsdk.live.CommunicationInterface;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatus;
import com.flir.thermalsdk.live.connectivity.ConnectionStatusListener;
import com.flir.thermalsdk.live.discovery.DiscoveryEventListener;
import com.flir.thermalsdk.live.discovery.DiscoveryFactory;
import com.flir.thermalsdk.live.streaming.ThermalImageStreamListener;
import com.flir.thermalsdk.log.ThermalLog;

public class MyCameraManager {

    private static final String TAG = "CameraActivity";
    private Camera flirCamera;
    private ConnectionStatus connectionStatus;
    private Context cameraActivityContext;
    private boolean hasBeenInitialized = false;
    private ThermalImagelistener thermalImagelistener;

    private static MyCameraManager instance;

    private MyCameraManager() {}

    /**
     *  {@link MyCameraManager} is a singleton to avoid multiple connections to the Flir camera
     * Remember to initialize the singleton by calling {@link #Init(Context)}  method
     * @return The instance of @MyCameraManager
     */
    public static MyCameraManager getInstance() {
        if (instance == null) {
            instance = new MyCameraManager();
            return instance;
        } else {
            return instance;
        }
    }

    /**
     * Initialize method for this singleton the method can be called many time but will only be initiated once.
     * @param applicationContext The context of CameraActivity
     */
    public void Init(Context applicationContext) {
        if(!hasBeenInitialized) {
            ThermalLog.LogLevel enableLoggingInDebug = BuildConfig.DEBUG ? ThermalLog.LogLevel.DEBUG : ThermalLog.LogLevel.NONE;
            ThermalSdkAndroid.init(applicationContext, enableLoggingInDebug);
            flirCamera = new Camera();
        }
    }

    public void subscribeToFlirCamera(ThermalImagelistener thermalImagelistener){
        DiscoveryFactory.getInstance().scan(aDiscoveryEventListener, CommunicationInterface.USB);
        this.thermalImagelistener = thermalImagelistener;
    }

    private void updateThermalListener(ThermalImage thermalImage){
        this.thermalImagelistener.subscribe(thermalImage);
    }

    //---------- below is Flir's crappy setup code ----------//
    /**
     * Note it is call on a non-UI thread
     */
    private final Camera.Consumer<ThermalImage> handleIncomingThermalImage = this::updateThermalListener;

    private ThermalImageStreamListener thermalImageStreamListener = () -> {
        //THIS IS WEIRD!?
        flirCamera.withImage(this.thermalImageStreamListener, handleIncomingThermalImage);
    };

    private final ConnectionStatusListener connectionStatusListener = (connectionStatus, errorCode)->{
            switch (connectionStatus){
                case CONNECTING:
                case DISCONNECTING:
                case DISCONNECTED:
                    this.connectionStatus = connectionStatus;
                    break;
                case CONNECTED:
                    this.connectionStatus = connectionStatus;
                    flirCamera.subscribeStream(thermalImageStreamListener);
                    break;
            }
    };

    private DiscoveryEventListener aDiscoveryEventListener = new DiscoveryEventListener() {
        @Override
        public void onCameraFound(Identity identity) {
            flirCamera.connect(identity, connectionStatusListener);
        }

        @Override
        public void onDiscoveryError(CommunicationInterface communicationInterface, ErrorCode errorCode) {
            Log.e(TAG, "onDiscoveryError: " + errorCode + " interface: " + communicationInterface);
        }
    };
}
