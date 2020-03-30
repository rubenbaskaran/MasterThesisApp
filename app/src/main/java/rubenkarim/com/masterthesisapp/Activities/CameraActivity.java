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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.flir.thermalsdk.ErrorCode;
import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.androidsdk.live.connectivity.UsbPermissionHandler;
import com.flir.thermalsdk.image.ImageFactory;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.fusion.FusionMode;
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
import rubenkarim.com.masterthesisapp.Managers.MyCameraManager.FlirConnectionListener;
import rubenkarim.com.masterthesisapp.Managers.MyCameraManager.MyCameraManager;
import rubenkarim.com.masterthesisapp.Managers.PermissionsManager.PermissionListener;
import rubenkarim.com.masterthesisapp.Managers.PermissionsManager.PermissionManager;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.Animation;
import rubenkarim.com.masterthesisapp.Utilities.GlobalVariables;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;
import rubenkarim.com.masterthesisapp.Utilities.Logging;
import rubenkarim.com.masterthesisapp.Utilities.MinMaxDataTransferContainer;

public class CameraActivity extends AppCompatActivity {

    //region Properties
    private static final String TAG = CameraActivity.class.getSimpleName();
    private boolean useDefaultImage = false;
    private View rootView;
    private MyCameraManager myCameraManager;
    private PermissionManager permissionManager;
    private String thermalImagePath;
    private ImageView imageView_cameraPreviewContainer;
    private ImageView imageView_faceTemplate;
    private RelativeLayout relativeLayout_eyeNoseTemplate;
    private ProgressBar progressBar_loadingAnimation;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        rootView = findViewById(R.id.linearLayout_CameraActivity);
        relativeLayout_eyeNoseTemplate = findViewById(R.id.relativeLayout_eyeNoseTemplate);
        imageView_faceTemplate = findViewById(R.id.imageView_faceTemplate);
        progressBar_loadingAnimation = findViewById(R.id.progressBar_cameraViewLoadingAnimation);
        imageView_cameraPreviewContainer = findViewById(R.id.imageView_cameraPreviewContainer);

        if (GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.MinMaxTemplate) {
            relativeLayout_eyeNoseTemplate.setVisibility(View.VISIBLE);
            imageView_faceTemplate.setVisibility(View.INVISIBLE);
        }

        permissionManager = new PermissionManager();
        myCameraManager = new MyCameraManager(getApplicationContext());

        checkPermissions(getListOfUsbDevices());
    }

    private HashMap<String, UsbDevice> getListOfUsbDevices() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        return manager.getDeviceList();
    }

    private void checkPermissions(HashMap<String, UsbDevice> deviceList) {
        if (PermissionManager.checkPermissions(this, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            startCameraPreview(deviceList);
        }
        else {
            permissionManager.requestPermissions(this, new PermissionListener() {
                        @Override
                        public void permissionGranted(String[] permissions) {
                            Snackbar.make(rootView, "permissions allowed", Snackbar.LENGTH_SHORT).show();
                            startCameraPreview(deviceList);
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startCameraPreview(HashMap<String, UsbDevice> deviceList) {
        if (!deviceList.isEmpty()) {
            Snackbar.make(rootView, "FLIR camera detected. Trying to connect", Snackbar.LENGTH_SHORT).show();
            setupFlirCamera();
        }
        else {
            Snackbar.make(rootView, "Can't find FLIR camera. Using default image", Snackbar.LENGTH_SHORT).show();
            setupDefaultImage();
        }
    }

    private void setupFlirCamera() {
        myCameraManager.InitCameraSearchAndSub((thermalImage) -> {
            //The image must not be processed on the UI Thread
            JavaImageBuffer javaImageBuffer = thermalImage.getImage();
            final Bitmap bitmap = BitmapAndroid.createBitmap(javaImageBuffer).getBitMap();

            runOnUiThread(() -> {
                imageView_cameraPreviewContainer.setImageBitmap(bitmap);
            });
        });

        myCameraManager.subscribeToFlirConnectionStatus(new FlirConnectionListener() {
            @Override
            public void onConnected(ConnectionStatus connectionStatus) {
                runOnUiThread(() -> {
                    Snackbar.make(rootView, "Camera connected", Snackbar.LENGTH_SHORT).show();
                });
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

    private void setupDefaultImage() {
        try {
            String defaultImageName = "Thermal_Test_Img1.jpg";
            ThermalImageFile thermalImageFile = (ThermalImageFile) ImageFactory.createImage(getAssets().open(defaultImageName));
            thermalImageFile.getFusion().setFusionMode(
                    GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.RgbThermalMapping ?
                            FusionMode.VISUAL_ONLY
                            : FusionMode.THERMAL_ONLY
            );
            thermalImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Masterthesisimages/" + defaultImageName;
            thermalImageFile.saveAs(thermalImagePath);
            imageView_cameraPreviewContainer.setImageBitmap(ImageProcessing.convertToBitmap(thermalImagePath));
            useDefaultImage = true;
        }
        catch (IOException e) {
            Snackbar.make(rootView, "an error accrued when open default image", Snackbar.LENGTH_SHORT).show();
            Logging.error("startCameraPreview", e);
        }
    }

    public void takePictureOnClick(View view) {
        // Fix for Android Studio bug (returning to previous activity on "stop app")
        if (GlobalVariables.getCurrentAlgorithm() == null) {
            Snackbar.make(rootView, "Error - Algorithm not selected", Snackbar.LENGTH_SHORT).show();
            return;
        }

        Animation.showLoadingAnimation(progressBar_loadingAnimation, imageView_faceTemplate, relativeLayout_eyeNoseTemplate);
        saveThermalImage();
    }

    private void saveThermalImage() {
        if (useDefaultImage) {
            goToMarkerActivity();
        }
        else {
            myCameraManager.addThermalImageListener((thermalImage) -> {
                File ImageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Masterthesisimages/");
                boolean isDirectoryCreated = ImageDir.exists() || ImageDir.mkdirs();
                try {
                    if (isDirectoryCreated) {
                        String fileName = new SimpleDateFormat("HH:mm:ss").format(new Timestamp(System.currentTimeMillis())) + "Thermal";
                        thermalImagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Masterthesisimages/" + fileName;
                        thermalImage.saveAs(thermalImagePath);
                        goToMarkerActivity();
                    }
                    else {
                        Log.i(TAG, "saveThermalImage: ERROR! IMAGE DIR NOT CREATED");
                        Animation.hideLoadingAnimation(progressBar_loadingAnimation, imageView_faceTemplate, relativeLayout_eyeNoseTemplate);
                        throw new IOException("Image Directory not created");
                    }
                }
                catch (IOException e) {
                    Log.d(TAG, "saveThermalImage: ERROR: " + e);
                    Animation.hideLoadingAnimation(progressBar_loadingAnimation, imageView_faceTemplate, relativeLayout_eyeNoseTemplate);
                }
            });
        }
    }

    private void goToMarkerActivity() {
        int[] coordinates = new int[2];
        imageView_cameraPreviewContainer.getLocationOnScreen(coordinates);
        int imageViewVerticalOffset = coordinates[1];
        int imageHeight = imageView_cameraPreviewContainer.getHeight();
        int imageWidth = imageView_cameraPreviewContainer.getWidth();

        Intent intent = new Intent(getApplicationContext(), MarkerActivity.class);
        intent.putExtra("thermalImagePath", thermalImagePath);
        intent.putExtra("imageViewVerticalOffset", imageViewVerticalOffset);
        intent.putExtra("imageHeight", imageHeight);
        intent.putExtra("imageWidth", imageWidth);
        addMinMaxDataIfChosen(intent);

        startActivity(intent);
    }

    private void addMinMaxDataIfChosen(Intent intent) {
        if (GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.MinMaxTemplate) {
            MinMaxDataTransferContainer minMaxDataTransferContainer = new MinMaxDataTransferContainer(
                    findViewById(R.id.imageView_leftEye),
                    findViewById(R.id.imageView_RightEye),
                    findViewById(R.id.imageView_Nose),
                    findViewById(R.id.imageView_cameraPreviewContainer));

            Bundle bundle = new Bundle();
            bundle.putSerializable("minMaxData", minMaxDataTransferContainer);
            intent.putExtras(bundle);
        }
    }

    public void backOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        if (myCameraManager != null) {
            myCameraManager.close();
            myCameraManager = null;
        }
        super.onPause();
    }
}