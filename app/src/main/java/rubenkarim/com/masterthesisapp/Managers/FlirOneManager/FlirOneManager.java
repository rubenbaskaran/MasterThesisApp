package rubenkarim.com.masterthesisapp.Managers.FlirOneManager;

import android.content.Context;
import android.util.Log;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.ThermalSdkAndroid;
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
import com.flir.thermalsdk.image.ThermalImage;
import com.flir.thermalsdk.live.Camera;
import com.flir.thermalsdk.live.CommunicationInterface;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatusListener;
import com.flir.thermalsdk.live.discovery.DiscoveryEventListener;
import com.flir.thermalsdk.live.discovery.DiscoveryFactory;
import com.flir.thermalsdk.live.remote.Battery;
import com.flir.thermalsdk.live.remote.Calibration;
import com.flir.thermalsdk.live.streaming.ThermalImageStreamListener;
import com.flir.thermalsdk.log.ThermalLog;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera.BatteryInfoListener;
import rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera.IThermalCamera;
import rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera.StatusListener;
import rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera.ThermalImagelistener;
import rubenkarim.com.masterthesisapp.Utilities.Logging;

public class FlirOneManager implements IThermalCamera {
    private static final String TAG = FlirOneManager.class.getSimpleName();
    private Camera flirCamera;
    private ArrayList<ThermalImagelistener> thermalImageListeners;
    private StatusListener statusListener;
    private Context appContext;

    public FlirOneManager(Context appContext) {
        this.appContext = appContext;
        ThermalLog.LogLevel enableLoggingInDebug =ThermalLog.LogLevel.DEBUG;
        ThermalSdkAndroid.init(appContext, enableLoggingInDebug);
        flirCamera = new Camera();
        thermalImageListeners = new ArrayList<>();
    }

    @Override
    public void subscribeToThermalImage(ThermalImagelistener thermalImagelistener){
        this.thermalImageListeners.add(thermalImagelistener);
    }

    @Override
    public void initCameraSearchAndSub(ThermalImagelistener thermalImagelistener){
        DiscoveryFactory.getInstance().scan(discoveryEventListener, CommunicationInterface.USB);
        this.thermalImageListeners.add(thermalImagelistener);
    }

    @Override
    public void subscribeToConnectionStatus(StatusListener statusListener){
        this.statusListener = statusListener;
    }

    private void updateThermalListener(ThermalImage thermalImage){
        for (ThermalImagelistener t: this.thermalImageListeners) {
            t.subscribe(thermalImage);
        }
    }

    @Override
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

    @Override
    public void calibrateCamera() throws NullPointerException {
        Logging.info(appContext,"FLIRONE", "is calibrating");
        flirCamera.getRemoteControl().getCalibration().nuc();
        try {
            flirCamera.getRemoteControl().getCalibration().subscribeCalibrationState(new Calibration.NucStateListener() {
                @Override
                public void onNucState(boolean b) {
                    statusListener.isCalibrating(b);
                }
            });
        } catch (Exception e) {
            Logging.error(appContext,TAG + "calibrateCamera", e);
        }

    }

    public int getBatteryPercentage() throws NullPointerException{
        return flirCamera.getRemoteControl().getBattery().getPercentage();
    }

    @Override
    public void subscribeToBatteryInfo(BatteryInfoListener batteryInfoListener){
        try {
            flirCamera.getRemoteControl().getBattery().subscribePercentage(i -> batteryInfoListener.BatteryPercentageUpdate(i));

            flirCamera.getRemoteControl().getBattery().subscribeChargingState(new Battery.BatteryStateListener() {
                @Override
                public void onStateChange(Battery.ChargingState chargingState) {
                    switch (chargingState){
                        case MANAGED_CHARGING:
                        case MANAGED_CHARGING_ONLY:
                            batteryInfoListener.isCharging(true);
                        case NO_CHARGING:
                            batteryInfoListener.isCharging(false);
                    }
                }
            });

        } catch (Exception e) {
            batteryInfoListener.subscriptionError(e);
        }
    }

    //region ---------- Flir's setup code ----------
    private ThermalImageStreamListener thermalImageStreamListener = () -> {
        flirCamera.withImage(this::updateThermalListener);
    };

    private final ConnectionStatusListener connectionStatusListener = new ConnectionStatusListener() {
        @Override
        public void onDisconnected(ErrorCode errorCode) {
            statusListener.onDisconnected(errorCode);
        }
    };

    private DiscoveryEventListener discoveryEventListener = new DiscoveryEventListener() {
        @Override
        public void onCameraFound(Identity identity) {
            connectToFlir(identity);

            if(UsbPermissionHandler.isFlirOne(identity)){
                if (UsbPermissionHandler.hasFlirOnePermission(identity, appContext)){
                    connectToFlir(identity);
                } else {

                    new UsbPermissionHandler().requestFlirOnePermisson(identity, appContext, new UsbPermissionHandler.UsbPermissionListener() {
                        @Override
                        public void permissionGranted(@NonNull Identity identity) {
                            connectToFlir(identity);
                        }

                        @Override
                        public void permissionDenied(@NonNull Identity identity) {
                            if(statusListener != null){
                                statusListener.permissionDenied(identity);
                            }
                        }

                        @Override
                        public void error(ErrorType errorType, Identity identity) {
                            if(statusListener != null){
                                statusListener.permissionError(errorType, identity);
                            }
                        }
                    });


                }
            }

        }

        @Override
        public void onDiscoveryError(CommunicationInterface communicationInterface, ErrorCode errorCode) {
            Logging.info(appContext, TAG , " onDiscoveryError: "+ errorCode.toString());
            Log.e(TAG, "onDiscoveryError: " + errorCode + " interface: " + communicationInterface);
        }
    };

    private void connectToFlir(Identity identity){
        Logging.info(appContext, TAG, "connecting to camera");

        try {
            flirCamera.connect(identity, connectionStatusListener);
            flirCamera.subscribeStream(thermalImageStreamListener);
            DiscoveryFactory.getInstance().stop();
        } catch (IOException e) {
            statusListener.onConnectionError(e);
        }



        if(statusListener != null){
            statusListener.cameraFound(identity);
        }
    }
    //endregion


}
