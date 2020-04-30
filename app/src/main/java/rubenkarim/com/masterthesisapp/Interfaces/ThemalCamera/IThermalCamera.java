package rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera;

public interface IThermalCamera {

    void initCameraSearchAndSub(ThermalImagelistener thermalImagelistener);

    void subscribeToThermalImage(ThermalImagelistener thermalImagelistener);

    void subscribeToConnectionStatus(StatusListener statusListener);

    void calibrateCamera() throws NullPointerException;

    int getBatteryPercentage();

    void subscribeToBatteryInfo(BatteryInfoListener batteryInfoListener);

    void close();
}
