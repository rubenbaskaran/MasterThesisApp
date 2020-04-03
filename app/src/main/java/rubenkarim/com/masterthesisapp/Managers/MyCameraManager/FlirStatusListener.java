package rubenkarim.com.masterthesisapp.Managers.MyCameraManager;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
import com.flir.thermalsdk.live.Identity;

import java.io.IOException;

public interface FlirStatusListener {
    void onDisconnected(ErrorCode errorCode);

    void identityFound(Identity identity);

    void permissionError(UsbPermissionHandler.UsbPermissionListener.ErrorType errorType, Identity identity);

    void permissionDenied(Identity identity);

    void onError(IOException e);

    void isCalibrating(boolean isCalibrating);
}
