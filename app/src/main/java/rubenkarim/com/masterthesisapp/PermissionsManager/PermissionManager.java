package rubenkarim.com.masterthesisapp.PermissionsManager;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class PermissionManager implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = PermissionManager.class.getSimpleName();
    private PermissionListener permissionListener;
    private final int PERMISSIONS_REQUEST_CODE = 10;

    public static boolean checkPermissions(Context context, String... permissionTypes){
        ArrayList<Integer> permissionsStats = new ArrayList<>();
        for (String permissionType: permissionTypes) {
            permissionsStats.add(ActivityCompat.checkSelfPermission(context, permissionType));
        }
        for (Integer i: permissionsStats
             ) {
            if (i != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "A permission has NOT been granted.");
                return false;
            }
        }
        Log.i(TAG, "All permission has already been granted.");
        return true;
    }



    public void requestPermissions(Activity activity, PermissionListener permissionListener, String... permissions){
        Log.i(TAG, "requestPermissions for " + Arrays.toString(permissions));
        this.permissionListener = permissionListener;
            ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    PERMISSIONS_REQUEST_CODE
            );
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionsResult: HELLO " + requestCode);
        if(requestCode == PERMISSIONS_REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if(this.permissionListener != null){
                    Log.i(TAG, "onRequestPermissionsResult: user allowed all permissions");
                    this.permissionListener.permissionGranted(permissions);
                }
            } else {
                if(this.permissionListener != null){
                    Log.i(TAG, "onRequestPermissionsResult: user denied som permissions");
                    this.permissionListener.permissionDenied(permissions);
                }
            }
        }
    }
}
