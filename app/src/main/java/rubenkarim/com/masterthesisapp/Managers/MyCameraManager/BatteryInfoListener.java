package rubenkarim.com.masterthesisapp.Managers.MyCameraManager;

interface BatteryInfoListener {
    void BatteryPercentageUpdate(int percentage);
    void subscriptionError(Exception e);
}
