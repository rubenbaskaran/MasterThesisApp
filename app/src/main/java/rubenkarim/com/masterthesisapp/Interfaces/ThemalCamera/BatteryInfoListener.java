package rubenkarim.com.masterthesisapp.Interfaces.ThemalCamera;

public interface BatteryInfoListener {
    void BatteryPercentageUpdate(int percentage);
    void subscriptionError(Exception e);
    void isCharging(boolean bool);
}
