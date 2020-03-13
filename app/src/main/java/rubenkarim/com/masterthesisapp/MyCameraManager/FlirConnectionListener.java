package rubenkarim.com.masterthesisapp.MyCameraManager;

import com.flir.thermalsdk.live.connectivity.ConnectionStatus;

public interface FlirConnectionListener {
    void onConncetion(ConnectionStatus connectionStatus);
    void onDisconnection(ConnectionStatus  connectionStatus);
}
