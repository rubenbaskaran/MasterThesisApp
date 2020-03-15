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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

import androidx.camera.core.ImageProxy;

public class MyCameraManager {

    private static final String TAG = "CameraActivity";
    private Camera flirCamera;
    private ConnectionStatus connectionStatus;
    private ArrayList<ThermalImagelistener> thermalImageListeners;
    private FlirConnectionListener flirConnectionListener = null;

    public MyCameraManager(Context applicationContext) {
        ThermalLog.LogLevel enableLoggingInDebug = BuildConfig.DEBUG ? ThermalLog.LogLevel.DEBUG : ThermalLog.LogLevel.NONE;
        ThermalSdkAndroid.init(applicationContext, enableLoggingInDebug);
        flirCamera = new Camera();
        thermalImageListeners = new ArrayList<>();
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

    private void updateThermalListener(ThermalImage thermalImage){
        for (ThermalImagelistener t: this.thermalImageListeners) {
            t.subscribe(thermalImage);
        }
    }

    public void close() {
        if (flirCamera == null) {
            return;
        }
        if (flirCamera.isGrabbing()) {
            flirCamera.unsubscribeAllStreams();
        }
        flirCamera.disconnect();

        if(DiscoveryFactory.getInstance().isDiscovering()){
            DiscoveryFactory.getInstance().stop();
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
            DiscoveryFactory.getInstance().stop(CommunicationInterface.USB);
        }

        @Override
        public void onDiscoveryError(CommunicationInterface communicationInterface, ErrorCode errorCode) {
            Log.e(TAG, "onDiscoveryError: " + errorCode + " interface: " + communicationInterface);
        }
    };
    //endregion


}
