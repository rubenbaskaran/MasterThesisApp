package rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera;

public interface BatteryInfoListener {
    void batteryPercentageUpdate(int percentage);
    void subscriptionError(Exception exception);
    void isCharging(boolean bool);
}
