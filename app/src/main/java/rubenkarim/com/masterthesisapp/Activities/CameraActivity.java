package rubenkarim.com.masterthesisapp.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;

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
    private static final String TAG = CameraActivity.class.getSimpleName();
    private View rootView;
    private CameraView cameraViewFinder;
    private boolean isThermalCameraOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        rootView = findViewById(R.id.linearLayout_CameraActivity);
        cameraViewFinder = findViewById(R.id.cameraView_RgbViewFinder);

        //Setup camera manager
        MyCameraManager.getInstance().Init(getApplicationContext());

        //CheckforUsbDevice
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        //Check Permissions:
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            if(!deviceList.isEmpty()){
                Snackbar.make(rootView, "USB device is detected trying to connect", Snackbar.LENGTH_SHORT).show();
                showThermalViewfinder();
                flirCamera();
            } else {
                Snackbar.make(rootView, "Cant find USB device opening phones camera", Snackbar.LENGTH_SHORT).show();
                showNativeCamera();
            }
        }
    }


//TODO: Handle Camera Disconnection and disconnect camera on change of activity!!!

    private void showNativeCamera(){
        ImageView imageView = findViewById(R.id.imageView_thermalViewFinder);
        if(imageView.getVisibility() == View.VISIBLE){
            imageView.setVisibility(View.GONE);
        }
        isThermalCameraOn = false;
        cameraViewFinder.setVisibility(View.VISIBLE);
        cameraViewFinder.bindToLifecycle(this);
        Log.i(TAG, "showNativeCamera: Showing Native Camera");
    }

    private void showThermalViewfinder(){
        if(cameraViewFinder.getVisibility() == View.VISIBLE){
            cameraViewFinder.setVisibility(View.GONE);
        }
        ImageView imageView = findViewById(R.id.imageView_thermalViewFinder);
        imageView.setVisibility(View.VISIBLE);
        isThermalCameraOn = true;
    }

    private void flirCamera(){
        MyCameraManager.getInstance().InitCameraSearchAndSub((thermalImage)->{
            //The image must not be processed on the UI Thread
            final ImageView flir_ViewFinder = findViewById(R.id.imageView_thermalViewFinder);
            JavaImageBuffer javaImageBuffer= thermalImage.getImage();
            final Bitmap bitmap = BitmapAndroid.createBitmap(javaImageBuffer).getBitMap();

            runOnUiThread(()->{
                flir_ViewFinder.setImageBitmap(bitmap);
            });
        });
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

    public void backOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void takePictureOnClick(View view) {
        if(isThermalCameraOn){
            takeAndSaveThermalImage();
        } else {
            takeAndSaveRGBImage();
        }
    }

    private void takeAndSaveThermalImage(){

        MyCameraManager.getInstance().addThermalImageListener((thermalImage)->{
            File ImageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Masterthesisimages/");
            boolean isDirectoryCreated = ImageDir.exists() || ImageDir.mkdirs();
            try{
            if (isDirectoryCreated) {
                String fileName = new SimpleDateFormat("HH:mm:ss").format(new Timestamp(System.currentTimeMillis())) + "Thermal";
                String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Masterthesisimages/" + fileName;
                thermalImage.saveAs(filepath);
                goToMarkerActivity(filepath, isThermalCameraOn);
            } else {

                    throw new IOException("Image Directory not created");

            }
            } catch (IOException e) {
                Log.d(TAG, "takeAndSaveThermalImage: ERROR: " + e);
            }

        });


    }

    private void takeAndSaveRGBImage(){
        File mImageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Masterthesisimages/");
        boolean isDirectoryCreated = mImageDir.exists() || mImageDir.mkdirs();

        if(isDirectoryCreated){

            String fileName = new SimpleDateFormat("HH:mm:ss").format(new Timestamp(System.currentTimeMillis()));
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Masterthesisimages", fileName+".jpg");


            cameraViewFinder.takePicture(file, Runnable::run, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Log.i(TAG, "onImageSaved: Picture saved! path: " + file.getPath());
                    goToMarkerActivity(file.getPath(), isThermalCameraOn);
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

    private void goToMarkerActivity(String imageFilePath, boolean isThermalImage) {
        Intent intent = new Intent(getApplicationContext(), MarkerActivity.class);
        intent.putExtra("isThermalImage", isThermalImage);
        intent.putExtra("filename", imageFilePath);
        startActivity(intent);
    }
}
