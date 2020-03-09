package rubenkarim.com.masterthesisapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import rubenkarim.com.masterthesisapp.R;

public class CameraActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 10;
    private static final String TAG= "CameraActivity";
    private View rootView;
    private View cameraViewFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        rootView = findViewById(R.id.linearLayout_CameraActivity);
        cameraViewFinder = findViewById(R.id.textureView_viewFinder);

        //Check Permissions:
        if(!checkCameraPermissions()){
            requestCameraPermissions();
        } else {
            findAndOpenAndroidCamera();
        }

    }

    private void findAndOpenAndroidCamera() {

    }



    private boolean checkCameraPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        // Check if the Camera permission is already available.
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.
            Log.i(TAG, "CAMERA permission has NOT been granted.");
            return false;
        } else {
            // Camera permissions are available.
            Log.i(TAG, "CAMERA permission has already been granted.");
            return true;
        }
    }

    private void requestCameraPermissions() {
        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Log.i(TAG, "Displaying camera permission rationale to provide additional context.");
            Snackbar.make(rootView, R.string.camera_permission_rationale, Snackbar
                    .LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {


                        @Override
                        public void onClick(View view) {
                            // Request Camera permission
                            ActivityCompat.requestPermissions(CameraActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting camera permission");
            // Request Camera permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    public void BackOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void TakePictureOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MarkerActivity.class);
        startActivity(intent);
    }
}
