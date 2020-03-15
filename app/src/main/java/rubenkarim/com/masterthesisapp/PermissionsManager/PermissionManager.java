package rubenkarim.com.masterthesisapp.PermissionsManager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class PermissionManager {

    private static final String TAG = PermissionManager.class.getSimpleName();

    public static boolean checkPermission(Context context, String permissionType){
        int permissionState = ActivityCompat.checkSelfPermission(context, permissionType);

        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "CAMERA permission has NOT been granted.");
            return false;
        } else {
            Log.i(TAG, "CAMERA permission has already been granted.");
            return true;
        }
    }


}
