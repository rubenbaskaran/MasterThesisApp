package rubenkarim.com.masterthesisapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.view.CameraView;
import androidx.core.app.ActivityCompat;
import rubenkarim.com.masterthesisapp.R;

public class CameraActivity extends AppCompatActivity{
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 10;
    private static final String TAG = "CameraActivity";
    private View rootView;
    private CameraView cameraViewFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        rootView = findViewById(R.id.linearLayout_CameraActivity);

        //Check Permissions:
        if (!checkCameraPermissions()) {
            requestCameraPermissions();
        } else {
            findAndOpenAndroidCamera();
        }



    }

    private void findAndOpenAndroidCamera() {
        cameraViewFinder = findViewById(R.id.previewView_viewFinder);
        cameraViewFinder.bindToLifecycle(this);
    }


    private boolean checkCameraPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "CAMERA permission has NOT been granted.");
            return false;
        } else {
            Log.i(TAG, "CAMERA permission has already been granted.");
            return true;
        }
    }

    private void requestCameraPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Snackbar.make(rootView, R.string.camera_permission_rationale, Snackbar
                    .LENGTH_INDEFINITE)
                    .setAction(R.string.ok, view -> {
                        // Request Camera permission
                        ActivityCompat.requestPermissions(CameraActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                REQUEST_PERMISSIONS_REQUEST_CODE);
                    })
                    .show();
        } else {
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

        File mImageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Masterthesisimages/");
        boolean isDirectoryCreated = mImageDir.exists() || mImageDir.mkdirs();

        if(isDirectoryCreated){

            String fileName = new SimpleDateFormat("HH:mm:ss").format(new Timestamp(System.currentTimeMillis()));
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Masterthesisimages", fileName+".jpg");

            cameraViewFinder.takePicture(file, Runnable::run, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Log.i(TAG, "onImageSaved: Picture saved! path: " + file.getPath());
                    Intent intent = new Intent(getApplicationContext(), MarkerActivity.class);
                    intent.putExtra("filename", file.getPath());
                    startActivity(intent);
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.d(TAG, "onError: " + exception);
                }
            });

        } else {
            Log.e(TAG, "TakePictureOnClick: There is an error with creating dir!");
        }
    }
}
