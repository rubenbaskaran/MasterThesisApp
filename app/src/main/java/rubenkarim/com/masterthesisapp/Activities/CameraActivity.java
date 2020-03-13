package rubenkarim.com.masterthesisapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.BuildConfig;
import com.flir.thermalsdk.androidsdk.ThermalSdkAndroid;
import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.ThermalImage;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.fusion.Fusion;
import com.flir.thermalsdk.image.fusion.VisualOnly;
import com.flir.thermalsdk.live.Camera;
import com.flir.thermalsdk.live.CommunicationInterface;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatus;
import com.flir.thermalsdk.live.connectivity.ConnectionStatusListener;
import com.flir.thermalsdk.live.discovery.DiscoveryEventListener;
import com.flir.thermalsdk.live.discovery.DiscoveryFactory;
import com.flir.thermalsdk.live.streaming.ThermalImageStreamListener;
import com.flir.thermalsdk.log.ThermalLog;
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
import rubenkarim.com.masterthesisapp.MyCameraManager.MyCameraManager;
import rubenkarim.com.masterthesisapp.R;

public class CameraActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 10;
    private static final String TAG = "CameraActivity";
    private View rootView;
    private CameraView cameraViewFinder;
    private Camera flirCamera;
    private ConnectionStatus connectionStatus;

    @Override
    protected void onStop() {
        super.onStop();
        try {
        flirCamera.unsubscribeAllStreams();

            flirCamera.close();
        } catch (Exception e) {
            log(e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        rootView = findViewById(R.id.linearLayout_CameraActivity);

        MyCameraManager.getInstance().Init(getApplicationContext());

        //Check Permissions:
        if (!checkPermissions()) {
            requestPermissions();

        } else {
            //findAndOpenAndroidCamera();
            // findAndOpenFlirCamera();
            log("All permission granted");
        }

    }


    private void findAndOpenFlirCamera(){

    }


    private void findAndOpenAndroidCamera() {
        cameraViewFinder = findViewById(R.id.previewView_viewFinder);
        cameraViewFinder.bindToLifecycle(this);
    }


    private boolean checkPermissions() {
        int permissionStateCamera = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int permissionStateWriteStorage = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionStateReadStorage = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        //int permissionStateReadStorage = ActivityCompat.checkSelfPermission(this, Manifest.permission.);

        if (permissionStateCamera != PackageManager.PERMISSION_GRANTED) {
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
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.append(String.format("%s\n", s));

    }

    public void connectFlir(View view) {
        findAndOpenFlirCamera();
    }
}
