package rubenkarim.com.masterthesisapp.Managers.MyCameraManager;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
import com.flir.thermalsdk.live.Identity;

import java.io.IOException;

/**
 * Is an interface to listen to the different information from the flir camera
 */
public interface FlirStatusListener {
    /**
     * Is call when the camera is disconnected ether by user or another error
     * @param errorCode The errorCode describing the error(contains a descriptive text)
     */
    void onDisconnected(ErrorCode errorCode);

    /**
     * Is called when the USB camera as been found and a connection is on the way
     * @param identity the identity of the camera
     */
    void cameraFound(Identity identity);

    /**
     * Is called if there is an error with getting the permissions.
     * @param errorType The type of error
     * @param identity The identity of the usb device
     */
    void permissionError(UsbPermissionHandler.UsbPermissionListener.ErrorType errorType, Identity identity);

    /**
     * is called if the user denies permission to the usb
     * @param identity The identity of the usb camera
     */
    void permissionDenied(Identity identity);

    /**
     * Is call if there is an usb device but it cannot be connected as a flir camera
     * Is critical!
     * @param e
     */
    void onConnectionError(IOException e);

    /**
     * Is called when the camera is calibrating and when it is finished calibrating
     * @param isCalibrating is true when the camera is calibrating and false when is is done.
     */
    void isCalibrating(boolean isCalibrating);
}
