package rubenkarim.com.masterthesisapp.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatus;
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
import rubenkarim.com.masterthesisapp.Algorithms.MinMaxAlgorithm;
import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.Models.RoiModel;
import rubenkarim.com.masterthesisapp.MyCameraManager.FlirConnectionListener;
import rubenkarim.com.masterthesisapp.MyCameraManager.MyCameraManager;
import rubenkarim.com.masterthesisapp.PermissionsManager.PermissionListener;
import rubenkarim.com.masterthesisapp.PermissionsManager.PermissionManager;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.GlobalVariables;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = CameraActivity.class.getSimpleName();
    private View rootView;
    private CameraView cameraViewFinder;
    private boolean isThermalCameraOn = true;
    private MyCameraManager myCameraManager;
    private PermissionManager permissionManager;
    private String filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        rootView = findViewById(R.id.linearLayout_CameraActivity);
        cameraViewFinder = findViewById(R.id.cameraView_RgbViewFinder);
        myCameraManager = new MyCameraManager(getApplicationContext());
        permissionManager = new PermissionManager();

        //CheckforUsbDevice
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        //Check Permissions:
        if (PermissionManager.checkPermissions(this, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            startView(deviceList);
        }
        else {
            permissionManager.requestPermissions(this, new PermissionListener() {
                        @Override
                        public void permissionGranted(String[] permissions) {
                            Snackbar.make(rootView, "permissions allowed", Snackbar.LENGTH_SHORT).show();
                            startView(deviceList);
                        }

                        @Override
                        public void permissionDenied(String[] permissions) {
                            Snackbar.make(rootView, "crucial permissions have been denied come back to allow permissions", Snackbar.LENGTH_INDEFINITE).show();
                        }
                    },
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        SetupAlgorithm();
    }

    private void SetupAlgorithm() {
        switch (GlobalVariables.getCurrentAlgorithm()) {
            case CNN:
                // Add setup for CNN
                break;
            case CNNWithTransferLearning:
                // Add setup for CNN with transfer learning
                break;
            case RgbThermalMapping:
                // Add setup for RgbThermalMapping
                break;
            case MaxMinTemplate:
                RelativeLayout relativeLayout_minMaxTemplate = findViewById(R.id.relativeLayout_minMaxTemplate);
                ImageView imageView_head = findViewById(R.id.imageView_head);
                relativeLayout_minMaxTemplate.setVisibility(View.VISIBLE);
                imageView_head.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void ExecuteAlgorithm() {
        GradientModel gradientAndPositions = null;

        switch (GlobalVariables.getCurrentAlgorithm()) {
            case CNN:
                // Add execution for CNN
                break;
            case CNNWithTransferLearning:
                // Add execution for CNN with transfer learning
                break;
            case RgbThermalMapping:
                // Add execution for RgbThermalMapping
                break;
            case MaxMinTemplate:
                ImageView imageView_leftEye = findViewById(R.id.imageView_leftEye);
                ImageView imageView_rightEye = findViewById(R.id.imageView_RightEye);
                ImageView imageView_nose = findViewById(R.id.imageView_Nose);
                int[] leftEyeLocation = new int[2];
                int[] rightEyeLocation = new int[2];
                int[] noseLocation = new int[2];
                imageView_leftEye.getLocationOnScreen(leftEyeLocation);
                imageView_rightEye.getLocationOnScreen(rightEyeLocation);
                imageView_nose.getLocationOnScreen(noseLocation);
                MinMaxAlgorithm minMaxAlgorithm = new MinMaxAlgorithm(
                        filepath,
                        new RoiModel(leftEyeLocation, imageView_leftEye.getHeight(), imageView_leftEye.getWidth()),
                        new RoiModel(rightEyeLocation, imageView_rightEye.getHeight(), imageView_rightEye.getWidth()),
                        new RoiModel(noseLocation, imageView_nose.getHeight(), imageView_nose.getWidth())
                );
                gradientAndPositions = minMaxAlgorithm.getGradientAndPositions();
                break;
        }

        // TODO: pass gradientAndPositions
        goToMarkerActivity(filepath, isThermalCameraOn);
    }

    private void startView(HashMap<String, UsbDevice> deviceList) {
        if (!deviceList.isEmpty()) {
            Snackbar.make(rootView, "USB device is detected trying to connect", Snackbar.LENGTH_SHORT).show();
            showThermalViewfinder();
            flirCamera();
        }
        else {
            Snackbar.make(rootView, "Cant find USB device opening phones camera", Snackbar.LENGTH_SHORT).show();
            myCameraManager.close();
            showNativeCamera();
        }
    }

    @Override
    protected void onPause() {
        myCameraManager.close();
        myCameraManager = null;
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showNativeCamera() {
        ImageView imageView = findViewById(R.id.imageView_thermalViewFinder);
        if (imageView.getVisibility() == View.VISIBLE) {
            imageView.setVisibility(View.GONE);
        }
        isThermalCameraOn = false;
        cameraViewFinder.setVisibility(View.VISIBLE);
        cameraViewFinder.bindToLifecycle(this);
        Log.i(TAG, "showNativeCamera: Showing Native Camera");
    }

    private void showThermalViewfinder() {
        if (cameraViewFinder.getVisibility() == View.VISIBLE) {
            cameraViewFinder.setVisibility(View.GONE);
        }
        ImageView imageView = findViewById(R.id.imageView_thermalViewFinder);
        imageView.setVisibility(View.VISIBLE);
        isThermalCameraOn = true;
    }

    private void flirCamera() {
        myCameraManager.InitCameraSearchAndSub((thermalImage) -> {
            //The image must not be processed on the UI Thread
            final ImageView flir_ViewFinder = findViewById(R.id.imageView_thermalViewFinder);
            JavaImageBuffer javaImageBuffer = thermalImage.getImage();
            final Bitmap bitmap = BitmapAndroid.createBitmap(javaImageBuffer).getBitMap();

            runOnUiThread(() -> {
                flir_ViewFinder.setImageBitmap(bitmap);
            });
        });

        myCameraManager.subscribeToFlirConnectionStatus(new FlirConnectionListener() {
            @Override
            public void onConnected(ConnectionStatus connectionStatus) {
                runOnUiThread(() -> {

                });
                Snackbar.make(rootView, "Camera connected", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onDisconnected(ConnectionStatus connectionStatus, ErrorCode errorCode) {
                runOnUiThread(() -> {
                    if (!errorCode.getMessage().isEmpty()) {
                        Snackbar.make(rootView, "Disconnection Error: " + errorCode.getMessage(), Snackbar.LENGTH_INDEFINITE).show();
                        Log.i(TAG, "onDisconnection: ERROR: " + errorCode.toString());
                    }
                });


            }

            @Override
            public void onDisconnecting(ConnectionStatus connectionStatus) {

            }

            @Override
            public void onConnecting(ConnectionStatus connectionStatus) {
                runOnUiThread(() -> {
                    Snackbar.make(rootView, "Camera connecting", Snackbar.LENGTH_LONG).show();
                });

            }

            @Override
            public void identityFound(Identity identity) {
            }

            @Override
            public void permissionError(UsbPermissionHandler.UsbPermissionListener.ErrorType errorType, Identity identity) {
                runOnUiThread(() -> {
                    Snackbar.make(rootView, "Permission error: " + errorType.name(), Snackbar.LENGTH_INDEFINITE).show();
                });

            }

            @Override
            public void permissionDenied(Identity identity) {
                runOnUiThread(() -> {
                    Snackbar.make(rootView, "USB Permission is Denied", Snackbar.LENGTH_INDEFINITE).show();
                });
            }
        });


    }

    public void backOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void takePictureOnClick(View view) {
        if (isThermalCameraOn) {
            takeAndSaveThermalImage();
        }
        else {
            takeAndSaveRGBImage();
        }
    }

    private void takeAndSaveThermalImage() {

        myCameraManager.addThermalImageListener((thermalImage) -> {
            File ImageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Masterthesisimages/");
            boolean isDirectoryCreated = ImageDir.exists() || ImageDir.mkdirs();
            try {
                if (isDirectoryCreated) {
                    String fileName = new SimpleDateFormat("HH:mm:ss").format(new Timestamp(System.currentTimeMillis())) + "Thermal";
                    filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Masterthesisimages/" + fileName;
                    thermalImage.saveAs(filepath);
                    ExecuteAlgorithm();
                }
                else {
                    Log.i(TAG, "takeAndSaveThermalImage: ERROR! IMAGE DIR NOT CREATED");
                    throw new IOException("Image Directory not created");
                }
            }
            catch (IOException e) {
                Log.d(TAG, "takeAndSaveThermalImage: ERROR: " + e);
            }

        });


    }

    private void takeAndSaveRGBImage() {
        Snackbar.make(rootView, "Taking picture hold still", Snackbar.LENGTH_LONG).show();
        File mImageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Masterthesisimages/");
        boolean isDirectoryCreated = mImageDir.exists() || mImageDir.mkdirs();

        if (isDirectoryCreated) {

            String fileName = new SimpleDateFormat("HH:mm:ss").format(new Timestamp(System.currentTimeMillis()));
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Masterthesisimages", fileName + ".jpg");
            filepath = file.getPath();

            cameraViewFinder.takePicture(file, Runnable::run, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Log.i(TAG, "onImageSaved: Picture saved! path: " + filepath);
                    ExecuteAlgorithm();
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.e(TAG, "onError: " + exception);
                }
            });

        }
        else {
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
