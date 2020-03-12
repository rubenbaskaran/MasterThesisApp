package rubenkarim.com.masterthesisapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.live.CommunicationInterface;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.discovery.DiscoveryEventListener;
import com.flir.thermalsdk.live.discovery.DiscoveryFactory;
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
        if (!checkPermissions()) {
            requestPermissions();

        } else {
            findAndOpenAndroidCamera();
        }
    }

    private void findAndOpenFlirCamera(){

        DiscoveryEventListener aDiscoveryEventListener = new DiscoveryEventListener() {
            @Override
            public void onCameraFound(Identity identity) {
                // identity describes a device and is used to connect to device




                }

            @Override
            public void onDiscoveryError(CommunicationInterface communicationInterface, ErrorCode errorCode) {
                Log.e(TAG, "onDiscoveryError: " + errorCode + " interface: " + communicationInterface);
            }
        };

        DiscoveryFactory.getInstance().scan(aDiscoveryEventListener, CommunicationInterface.USB);
    }


    private void findAndOpenAndroidCamera() {
        cameraViewFinder = findViewById(R.id.previewView_viewFinder);
        cameraViewFinder.bindToLifecycle(this);
    }


    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (permissionState != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "CAMERA permission has NOT been granted.");
            return false;
        } else {
            Log.i(TAG, "CAMERA permission has already been granted.");
            log("Permission granted");
            return true;
        }
    }

    private void requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Snackbar.make(rootView, R.string.permission_rationale, Snackbar
                    .LENGTH_INDEFINITE)
                    .setAction(R.string.ok, view -> {
                        // Request Camera permission
                        ActivityCompat.requestPermissions(CameraActivity.this,
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_PERMISSIONS_REQUEST_CODE);
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(CameraActivity.this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
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
                    Log.e(TAG, "onError: " + exception);
                }
            });

        } else {
            Log.e(TAG, "TakePictureOnClick: There is an error with creating dir!");
        }
    }

    /**
     * temporary method
     * @param s text to be printed to screen
     */
    private void log(String s){
        TextView textView = findViewById(R.id.textView_log);
        textView.append(String.format("%s\n", s));

    }

}
