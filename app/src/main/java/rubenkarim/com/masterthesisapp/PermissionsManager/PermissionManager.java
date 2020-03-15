package rubenkarim.com.masterthesisapp.PermissionsManager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import rubenkarim.com.masterthesisapp.Activities.CameraActivity;
import rubenkarim.com.masterthesisapp.R;

public class PermissionManager implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = PermissionManager.class.getSimpleName();
    private PermissionListener permissionListener;
    private final int PERMISSIONS_REQUEST_CODE = 10;

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



    public void requestPermissions(Activity activity, PermissionListener permissionListener, String... permissions){
            ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    PERMISSIONS_REQUEST_CODE
            );
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(this.permissionListener != null){
                    this.permissionListener.permissionGranted(permissions);
                }
            } else {
                if(this.permissionListener != null){
                    this.permissionListener.permissionDenied(permissions);
                }
            }
        }
    }
}
