package rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera;

public interface IThermalCamera {
    void initCameraSearchAndSub(ThermalImageListener thermalImageListener);
    void subscribeToThermalImage(ThermalImageListener thermalImageListener);
    void subscribeToConnectionStatus(StatusListener statusListener);
    void calibrateCamera() throws NullPointerException;
    void subscribeToBatteryInfo(BatteryInfoListener batteryInfoListener);
    void close();
    void unSubscribeThermalImages();
}

