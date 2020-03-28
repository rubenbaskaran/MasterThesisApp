package rubenkarim.com.masterthesisapp.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
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
import com.flir.thermalsdk.live.Identity;
import com.flir.thermalsdk.live.connectivity.ConnectionStatus;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.view.CameraView;
import rubenkarim.com.masterthesisapp.Algorithms.MinMaxAlgorithm;
import rubenkarim.com.masterthesisapp.Managers.MyCameraManager.FlirConnectionListener;
import rubenkarim.com.masterthesisapp.Managers.MyCameraManager.MyCameraManager;
import rubenkarim.com.masterthesisapp.Managers.PermissionsManager.PermissionListener;
import rubenkarim.com.masterthesisapp.Managers.PermissionsManager.PermissionManager;
import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.Models.RoiModel;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.GlobalVariables;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = CameraActivity.class.getSimpleName();
    private View rootView;
    private CameraView cameraView_rgbViewFinder;
    private boolean isThermalCameraOn = false;
    private MyCameraManager myCameraManager;
    private PermissionManager permissionManager;
    private String filepath;
    private boolean useDefaultPicture = false;
    private ImageView imageView_thermalViewFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        rootView = findViewById(R.id.linearLayout_CameraActivity);
        cameraView_rgbViewFinder = findViewById(R.id.cameraView_rgbViewFinder);
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
        // Fix for Android Studio bug (returning to previous activity on "stop app")
        if (GlobalVariables.getCurrentAlgorithm() == null) {
            Snackbar.make(rootView, "Error - Algorithm not selected", Snackbar.LENGTH_SHORT).show();
            return;
        }

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

    // TODO: Add custom exceptions
    private void ExecuteAlgorithm() {
        // Fix for Android Studio bug (returning to previous activity on "stop app")
        if (GlobalVariables.getCurrentAlgorithm() == null) {
            Snackbar.make(rootView, "Error - Algorithm not selected", Snackbar.LENGTH_SHORT).show();
            backOnClick(null);
        }

        GradientModel gradientAndPositions = null;

        switch (GlobalVariables.getCurrentAlgorithm()) {
            case CNN:
                // Add execution for CNN
                break;
            case CNNWithTransferLearning:
                // Add execution for CNN with transfer learning
                break;
            case RgbThermalMapping:
                if (!isThermalCameraOn) {
                    useDefaultPicture = true;
                }
                detectFaces();
                break;
            case MaxMinTemplate:
                ImageView imageView_leftEye = findViewById(R.id.imageView_leftEye);
                ImageView imageView_rightEye = findViewById(R.id.imageView_RightEye);
                ImageView imageView_nose = findViewById(R.id.imageView_Nose);
                View cameraPreviewElement = isThermalCameraOn ? imageView_thermalViewFinder : cameraView_rgbViewFinder;
                int[] leftEyeLocation = new int[2];
                int[] rightEyeLocation = new int[2];
                int[] noseLocation = new int[2];
                int[] cameraPreviewLocation = new int[2];
                imageView_leftEye.getLocationOnScreen(leftEyeLocation);
                imageView_rightEye.getLocationOnScreen(rightEyeLocation);
                imageView_nose.getLocationOnScreen(noseLocation);
                cameraPreviewElement.getLocationOnScreen(cameraPreviewLocation);

                MinMaxAlgorithm minMaxAlgorithm = new MinMaxAlgorithm(
                        filepath,
                        new RoiModel(leftEyeLocation, imageView_leftEye.getHeight(), imageView_leftEye.getWidth()),
                        new RoiModel(rightEyeLocation, imageView_rightEye.getHeight(), imageView_rightEye.getWidth()),
                        new RoiModel(noseLocation, imageView_nose.getHeight(), imageView_nose.getWidth()),
                        new RoiModel(cameraPreviewLocation, cameraPreviewElement.getWidth(), cameraPreviewElement.getHeight())
                );
                gradientAndPositions = minMaxAlgorithm.getGradientAndPositions();
                goToMarkerActivity(filepath, gradientAndPositions);
                break;
        }
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
        if (myCameraManager != null) {
            myCameraManager.close();
            myCameraManager = null;
        }
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showNativeCamera() {
        imageView_thermalViewFinder = findViewById(R.id.imageView_thermalViewFinder);
        if (imageView_thermalViewFinder.getVisibility() == View.VISIBLE) {
            imageView_thermalViewFinder.setVisibility(View.GONE);
        }
        isThermalCameraOn = false;
        cameraView_rgbViewFinder.setVisibility(View.VISIBLE);
        cameraView_rgbViewFinder.bindToLifecycle(this);
        Log.i(TAG, "showNativeCamera: Showing Native Camera");
    }

    private void showThermalViewfinder() {
        if (cameraView_rgbViewFinder.getVisibility() == View.VISIBLE) {
            cameraView_rgbViewFinder.setVisibility(View.GONE);
        }
        imageView_thermalViewFinder = findViewById(R.id.imageView_thermalViewFinder);
        imageView_thermalViewFinder.setVisibility(View.VISIBLE);
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
        showLoadingAnimation();

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
                    hideLoadingAnimation();
                    throw new IOException("Image Directory not created");
                }
            }
            catch (IOException e) {
                Log.d(TAG, "takeAndSaveThermalImage: ERROR: " + e);
                hideLoadingAnimation();
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

            cameraView_rgbViewFinder.takePicture(file, Runnable::run, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    Log.i(TAG, "onImageSaved: Picture saved! path: " + filepath);
                    ExecuteAlgorithm();
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.e(TAG, "onError: " + exception);
                    hideLoadingAnimation();
                }
            });
        }
        else {
            Log.e(TAG, "TakePictureOnClick: There is an error with creating dir!");
            hideLoadingAnimation();
        }
    }

    private void goToMarkerActivity(String imageFilePath, GradientModel gradientAndPositions) {
        RelativeLayout relativeLayout_cameraPreview = findViewById(R.id.relativeLayout_cameraPreview);
        int[] coordinates = new int[2];
        relativeLayout_cameraPreview.getLocationOnScreen(coordinates);
        int imageViewVerticalOffset = coordinates[1];
        int imageHeight = relativeLayout_cameraPreview.getHeight();
        int imageWidth = relativeLayout_cameraPreview.getWidth();

        Intent intent = new Intent(getApplicationContext(), MarkerActivity.class);
        intent.putExtra("isThermalCameraOn", isThermalCameraOn);
        intent.putExtra("filename", imageFilePath);
        intent.putExtra("imageViewVerticalOffset", imageViewVerticalOffset);
        intent.putExtra("imageHeight", imageHeight);
        intent.putExtra("imageWidth", imageWidth);
        intent.putExtra("useDefaultPicture", useDefaultPicture);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gradientAndPositions", gradientAndPositions);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void detectFaces() {
        Bitmap imageBitmap = null;
        int horizontalOffset = 50;

        if (useDefaultPicture) {
            imageBitmap = ImageProcessing.convertDrawableToBitmap(this, R.drawable.rgb_picture);
        }
        else {
            if (ThermalImageFile.isThermalImage(filepath)) {
                try {
                    ImageProcessing.FixImageOrientation(filepath);
                    ThermalImageFile thermalImageFile = (ThermalImageFile) ImageFactory.createImage(filepath);
                    JavaImageBuffer rgbImage = thermalImageFile.getFusion().getPhoto();
                    imageBitmap = BitmapAndroid.createBitmap(rgbImage).getBitMap();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        FirebaseVisionImage image = ImageProcessing.convertToFirebaseVisionImage(imageBitmap);
        GradientModel gradientAndPositions = new GradientModel(100, null, null);

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                        .setMinFaceSize(0.15f)
                        .build();

        FirebaseVisionFaceDetector detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        Task<List<FirebaseVisionFace>> result =
                detector.detectInImage(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<FirebaseVisionFace>>() {
                                    @Override
                                    public void onSuccess(List<FirebaseVisionFace> faces) {
                                        // Task completed successfully
                                        // [START_EXCLUDE]
                                        // [START get_face_info]
                                        if (!faces.isEmpty()) {
                                            Rect bounds = faces.get(0).getBoundingBox();
                                            float rotY = faces.get(0).getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                            float rotZ = faces.get(0).getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                            //TODO: Get inside of eye instead of center
                                            FirebaseVisionFaceLandmark leftEye = faces.get(0).getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
                                            if (leftEye != null) {
                                                gradientAndPositions.setEyePosition(new int[]{(int) ((float) leftEye.getPosition().getX()), (int) ((float) leftEye.getPosition().getY()) + horizontalOffset});
                                            }
                                            FirebaseVisionFaceLandmark nose = faces.get(0).getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
                                            if (nose != null) {
                                                gradientAndPositions.setNosePosition(new int[]{(int) ((float) nose.getPosition().getX()), (int) ((float) nose.getPosition().getY()) + horizontalOffset});
                                            }

                                            goToMarkerActivity(filepath, gradientAndPositions);
                                        }
                                        else {
                                            Snackbar.make(rootView, "No faces found", Snackbar.LENGTH_SHORT).show();
                                            hideLoadingAnimation();
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootView, "Face detection error", Snackbar.LENGTH_SHORT).show();
                                        hideLoadingAnimation();
                                    }
                                });
    }

    private void showLoadingAnimation() {
        ProgressBar progressBar_loadingAnimation = findViewById(R.id.progressBar_loadingAnimation);
        if (progressBar_loadingAnimation.getVisibility() == View.INVISIBLE) {
            progressBar_loadingAnimation.setVisibility(View.VISIBLE);
        }

        if (GlobalVariables.getCurrentAlgorithm() != GlobalVariables.Algorithms.MaxMinTemplate) {
            ImageView headTemplate = findViewById(R.id.imageView_head);
            if (headTemplate.getVisibility() == View.VISIBLE) {
                headTemplate.setVisibility(View.INVISIBLE);
            }
        }

        if (GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.MaxMinTemplate) {
            RelativeLayout eyeNoseTemplate = findViewById(R.id.relativeLayout_minMaxTemplate);
            if (eyeNoseTemplate.getVisibility() == View.VISIBLE) {
                eyeNoseTemplate.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void hideLoadingAnimation() {
        ProgressBar progressBar_loadingAnimation = findViewById(R.id.progressBar_loadingAnimation);
        if (progressBar_loadingAnimation.getVisibility() == View.VISIBLE) {
            progressBar_loadingAnimation.setVisibility(View.INVISIBLE);
        }

        if (GlobalVariables.getCurrentAlgorithm() != GlobalVariables.Algorithms.MaxMinTemplate) {
            ImageView headTemplate = findViewById(R.id.imageView_head);
            if (headTemplate.getVisibility() == View.INVISIBLE) {
                headTemplate.setVisibility(View.VISIBLE);
            }
        }

        if (GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.MaxMinTemplate) {
            RelativeLayout eyeNoseTemplate = findViewById(R.id.relativeLayout_minMaxTemplate);
            if (eyeNoseTemplate.getVisibility() == View.INVISIBLE) {
                eyeNoseTemplate.setVisibility(View.VISIBLE);
            }
        }
    }
}
