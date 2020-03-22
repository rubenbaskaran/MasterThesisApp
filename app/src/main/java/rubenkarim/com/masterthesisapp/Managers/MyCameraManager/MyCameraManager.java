package rubenkarim.com.masterthesisapp.Managers.MyCameraManager;

import android.content.Context;
import android.util.Log;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.ThermalSdkAndroid;
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
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

import androidx.annotation.NonNull;

public class MyCameraManager {

    private static final String TAG = MyCameraManager.class.getSimpleName();
    private Camera flirCamera;
    private ConnectionStatus connectionStatus;
    private ArrayList<ThermalImagelistener> thermalImageListeners;
    private FlirConnectionListener flirConnectionListener;
    private Context applicationContext;

    public MyCameraManager(Context applicationContext) {
        this.applicationContext = applicationContext;
        ThermalLog.LogLevel enableLoggingInDebug =ThermalLog.LogLevel.DEBUG;
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
            Log.i(TAG, "close: CLOSED HAS BEEN CALLED!!!!!");
            if (flirCamera == null) {
                Log.i(TAG, "close: FLIRCAMER IS NULL CANT CLOSE");
                return;
            }
            if (flirCamera.isGrabbing()) {
                Log.i(TAG, "close: FLIRCAMER IS UNSUB STREAMS!");
                flirCamera.unsubscribeAllStreams();
            }
            flirCamera.disconnect();

            if(DiscoveryFactory.getInstance().isDiscovering()){
                Log.i(TAG, "close: IS CLOSING SCANNING!!!!!!");
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
                    if(this.flirConnectionListener != null){
                        this.flirConnectionListener.onConnecting(connectionStatus);
                    }
                case DISCONNECTING:
                    if(this.flirConnectionListener != null){
                        this.flirConnectionListener.onDisconnecting(connectionStatus);
                    }

                case CONNECTED:
                    if(this.flirConnectionListener != null){
                        this.flirConnectionListener.onConnected(connectionStatus);
                    }
                    flirCamera.subscribeStream(thermalImageStreamListener);
                    break;

                case DISCONNECTED:
                    if(this.flirConnectionListener != null){
                        if (errorCode != null && errorCode.getCode() != 0){
                            this.flirConnectionListener.onDisconnected(connectionStatus, errorCode);
                        }
                    }
            }
    };

    private DiscoveryEventListener aDiscoveryEventListener = new DiscoveryEventListener() {
        @Override
        public void onCameraFound(Identity identity) {
            connectToFlir(identity);

            if(UsbPermissionHandler.isFlirOne(identity)){
                if (UsbPermissionHandler.hasFlirOnePermission(identity, applicationContext)){
                    connectToFlir(identity);
                } else {

                    new UsbPermissionHandler().requestFlirOnePermisson(identity, applicationContext, new UsbPermissionHandler.UsbPermissionListener() {
                        @Override
                        public void permissionGranted(@NonNull Identity identity) {
                            connectToFlir(identity);
                        }

                        @Override
                        public void permissionDenied(@NonNull Identity identity) {
                            if(flirConnectionListener != null){
                                flirConnectionListener.permissionDenied(identity);
                            }
                        }

                        @Override
                        public void error(ErrorType errorType, Identity identity) {
                            if(flirConnectionListener != null){
                                flirConnectionListener.permissionError(errorType, identity);
                            }
                        }
                    });


                }
            }

        }

        @Override
        public void onDiscoveryError(CommunicationInterface communicationInterface, ErrorCode errorCode) {
            Log.e(TAG, "onDiscoveryError: " + errorCode + " interface: " + communicationInterface);
        }
    };

    private void connectToFlir(Identity identity) {
        flirCamera.connect(identity, connectionStatusListener);
        DiscoveryFactory.getInstance().stop();

        if(flirConnectionListener != null){
            flirConnectionListener.identityFound(identity);
        }


    }
    //endregion


}
