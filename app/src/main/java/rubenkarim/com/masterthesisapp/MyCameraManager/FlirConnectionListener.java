package rubenkarim.com.masterthesisapp.MyCameraManager;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatus;

public interface FlirConnectionListener {
    void onConnection(ConnectionStatus connectionStatus);
    void onDisconnection(ConnectionStatus connectionStatus, ErrorCode errorCode);

    void onDisconnecting(ConnectionStatus connectionStatus);

    void onConnecting(ConnectionStatus connectionStatus);

    void identityFound(Identity identity);

    void permissionError(UsbPermissionHandler.UsbPermissionListener.ErrorType errorType, Identity identity);

    void permissionDenied(Identity identity);
}
