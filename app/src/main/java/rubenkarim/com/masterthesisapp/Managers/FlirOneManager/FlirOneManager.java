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
import java.util.List;

import androidx.annotation.NonNull;
import rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera.BatteryInfoListener;
import rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera.IThermalCamera;
import rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera.StatusListener;
import rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera.ThermalImageListener;
import rubenkarim.com.masterthesisapp.Models.ThermalImgModel;
import rubenkarim.com.masterthesisapp.Utilities.Logging;

public class FlirOneManager implements IThermalCamera {
    private static final String TAG = FlirOneManager.class.getSimpleName();
    private Camera flirCamera;
    private List<ThermalImageListener> thermalImageListeners;
    private StatusListener statusListener;
    private Context appContext;
    private BatteryInfoListener mBatteryInfoListener;

    public FlirOneManager(Context appContext) {
        this.appContext = appContext;
        ThermalLog.LogLevel enableLoggingInDebug = ThermalLog.LogLevel.DEBUG;
        ThermalSdkAndroid.init(appContext, enableLoggingInDebug);
        //ThermalSdkAndroid.init(appContext);
        flirCamera = new Camera();
        thermalImageListeners = new ArrayList<>();
    }

    @Override
    public void subscribeToThermalImage(ThermalImageListener thermalImagelistener) {
        this.thermalImageListeners.add(thermalImagelistener);
    }

    @Override
    public void unSubscribeThermalImages() {
        this.thermalImageListeners.clear();
    }

    @Override
    public void initCameraSearchAndSub(ThermalImageListener thermalImagelistener) {
        DiscoveryFactory.getInstance().scan(discoveryEventListener, CommunicationInterface.USB);
        this.thermalImageListeners.add(thermalImagelistener);
    }

    @Override
    public void subscribeToConnectionStatus(StatusListener statusListener) {
        this.statusListener = statusListener;
    }

    private void updateThermalListener(ThermalImage thermalImage) {
        if (this.thermalImageListeners != null && !this.thermalImageListeners.isEmpty()) {
            for (ThermalImageListener t : this.thermalImageListeners) {
                t.subscribe(new ThermalImgModel(thermalImage));
            }
        }
    }

    @Override
    public void close() {
        this.thermalImageListeners = null;
        this.statusListener = null;
        this.mBatteryInfoListener = null;
        if (flirCamera == null) {
            return;
        }
        if (flirCamera.isGrabbing()) {
            flirCamera.unsubscribeAllStreams();
        }
        flirCamera.disconnect();
        flirCamera = null;

        if (DiscoveryFactory.getInstance().isDiscovering()) {
            DiscoveryFactory.getInstance().stop();
        }
    }

    @Override
    public void calibrateCamera() throws NullPointerException {
        Logging.info(appContext, "FLIRONE", "is calibrating");
        flirCamera.getRemoteControl().getCalibration().nuc();
        try {
            flirCamera.getRemoteControl().getCalibration().subscribeCalibrationState(new Calibration.NucStateListener() {
                @Override
                public void onNucState(boolean b) {
                    if (statusListener != null) {
                        statusListener.isCalibrating(b);
                    }
                }
            });
        } catch (Exception e) {
            Logging.error(appContext, TAG + "calibrateCamera", e);
        }

    }

    @Override
    public void subscribeToBatteryInfo(BatteryInfoListener batteryInfoListener) {
        this.mBatteryInfoListener = batteryInfoListener;
    }

    //region ---------- Flir's setup code ----------
    private ThermalImageStreamListener thermalImageStreamListener = () -> {
        flirCamera.withImage(this::updateThermalListener);
    };

    private final ConnectionStatusListener connectionStatusListener = new ConnectionStatusListener() {
        @Override
        public void onDisconnected(ErrorCode errorCode) {
            if (statusListener != null) {
                statusListener.onDisconnected(errorCode);
            }
        }
    };

    private DiscoveryEventListener discoveryEventListener = new DiscoveryEventListener() {
        @Override
        public void onCameraFound(Identity identity) {
            if (UsbPermissionHandler.isFlirOne(identity)) {
                if (UsbPermissionHandler.hasFlirOnePermission(identity, appContext)) {
                    connectToFlir(identity);
                } else {

                    new UsbPermissionHandler().requestFlirOnePermisson(identity, appContext, new UsbPermissionHandler.UsbPermissionListener() {
                        @Override
                        public void permissionGranted(@NonNull Identity identity) {
                            connectToFlir(identity);
                        }

                        @Override
                        public void permissionDenied(@NonNull Identity identity) {
                            if (statusListener != null) {
                                statusListener.permissionDenied(identity);
                            }
                        }

                        @Override
                        public void error(ErrorType errorType, Identity identity) {
                            if (statusListener != null) {
                                statusListener.permissionError(errorType, identity);
                            }
                        }
                    });


                }
            }

        }

        @Override
        public void onDiscoveryError(CommunicationInterface communicationInterface, ErrorCode errorCode) {
            Logging.info(appContext, TAG, " onDiscoveryError: " + errorCode.toString());
            Log.e(TAG, "onDiscoveryError: " + errorCode + " interface: " + communicationInterface);
        }
    };

    private void connectToFlir(Identity identity) {
        Logging.info(appContext, TAG, "connecting to camera");

        new Thread(() -> {
            try {
                flirCamera.connect(identity, connectionStatusListener);
                flirCamera.subscribeStream(thermalImageStreamListener);

                if (mBatteryInfoListener != null) {
                    try {
                        mBatteryInfoListener.batteryPercentageUpdate(flirCamera.getRemoteControl().getBattery().getPercentage());
                        flirCamera.getRemoteControl().getBattery().subscribePercentage(i -> mBatteryInfoListener.batteryPercentageUpdate(i));

                        flirCamera.getRemoteControl().getBattery().subscribeChargingState(new Battery.BatteryStateListener() {
                            @Override
                            public void onStateChange(Battery.ChargingState chargingState) {
                                switch (chargingState) {
                                    case MANAGED_CHARGING:
                                    case MANAGED_CHARGING_ONLY:
                                        mBatteryInfoListener.isCharging(true);
                                        break;
                                    case NO_CHARGING:
                                        mBatteryInfoListener.isCharging(false);
                                        break;
                                }
                            }
                        });

                    } catch (Exception e) {
                        mBatteryInfoListener.subscriptionError(e);
                    }
                }

            } catch (IOException e) {
                if (statusListener != null) {
                    statusListener.onConnectionError(e);
                }
            } finally {
                if (DiscoveryFactory.getInstance().isDiscovering()) {
                    DiscoveryFactory.getInstance().stop();
                }
            }
        }).start();

        if (statusListener != null) {
            statusListener.cameraFound(identity);
        }
    }
    //endregion


}
