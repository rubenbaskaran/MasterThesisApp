package rubenkarim.com.masterthesisapp.Managers.MyCameraManager;

public interface IThermalCamera {

    void InitCameraSearchAndSub(ThermalImagelistener thermalImagelistener);

    void SubscribeToThermalImage(ThermalImagelistener thermalImagelistener);

    void SubscribeToConnectionStatus(StatusListener statusListener);

    void calibrateCamera() throws NullPointerException;

    int getBatteryPercentage();

    void SubscribeToBatteryInfo(BatteryInfoListener batteryInfoListener);

    void close();
}
