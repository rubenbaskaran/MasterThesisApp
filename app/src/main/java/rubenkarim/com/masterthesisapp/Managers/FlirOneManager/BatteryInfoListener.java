package rubenkarim.com.masterthesisapp.Managers.FlirOneManager;

public interface BatteryInfoListener {
    void BatteryPercentageUpdate(int percentage);
    void subscriptionError(Exception e);
    void isCharging(boolean bool);
}
