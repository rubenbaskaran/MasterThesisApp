package rubenkarim.com.masterthesisapp.MyCameraManager;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.BuildConfig;
import com.flir.thermalsdk.androidsdk.ThermalSdkAndroid;
import com.flir.thermalsdk.image.Point;
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

import java.util.ArrayList;

import androidx.camera.core.ImageProxy;

public class MyCameraManager {

    private static final String TAG = "CameraActivity";
    private Camera flirCamera;
    private ConnectionStatus connectionStatus;
    private boolean hasBeenInitialized = false;
    private ArrayList<ThermalImagelistener> thermalImageListeners;
    private FlirConnectionListener flirConnectionListener = null;
    private UsbDevice usbDevice;
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
            hasBeenInitialized = true;
            thermalImageListeners = new ArrayList<>();
        }
    }

    public void addThermalImageListener(ThermalImagelistener thermalImagelistener){
        this.thermalImageListeners.add(thermalImagelistener);
    }

    public void InitCameraSearchAndSub(ThermalImagelistener thermalImagelistener){
        DiscoveryFactory.getInstance().scan(aDiscoveryEventListener, CommunicationInterface.USB);
        this.thermalImageListeners.add(thermalImagelistener);
    }

    public void subscribeToFlirConnectionStatus(FlirConnectionListener flirConnectionListener){
        this.flirConnectionListener = flirConnectionListener;
    }

    public double getTempFromPoint(ThermalImage thermalImage, int x, int y){
        return thermalImage.getValueAt(new Point(x, y));
    }

    private void updateThermalListener(ThermalImage thermalImage){
        for (ThermalImagelistener t: this.thermalImageListeners) {
            t.subscribe(thermalImage);
        }
    }

    //region ---------- Flir's crappy setup code ----------
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
                    if(this.flirConnectionListener != null){
                        this.flirConnectionListener.onDisconnection(connectionStatus);
                    }
                    break;
                case CONNECTED:
                    if(this.flirConnectionListener != null){
                        this.flirConnectionListener.onConncetion(connectionStatus);
                    }
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

    public void setUsbDevice(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
    }
    //endregion


}
