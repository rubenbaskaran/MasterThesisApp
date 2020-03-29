package rubenkarim.com.masterthesisapp.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
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
import com.flir.thermalsdk.image.ThermalImage;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Algorithms.Cnn;
import rubenkarim.com.masterthesisapp.Algorithms.MinMaxAlgorithm;
import rubenkarim.com.masterthesisapp.Managers.MyCameraManager.FlirConnectionListener;
import rubenkarim.com.masterthesisapp.Managers.MyCameraManager.MyCameraManager;
import rubenkarim.com.masterthesisapp.Managers.PermissionsManager.PermissionListener;
import rubenkarim.com.masterthesisapp.Managers.PermissionsManager.PermissionManager;
import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.Models.RoiModel;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.Animation;
import rubenkarim.com.masterthesisapp.Utilities.GlobalVariables;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;
import rubenkarim.com.masterthesisapp.Utilities.Logging;

public class CameraActivity extends AppCompatActivity {

    //region Properties
    private static final String TAG = CameraActivity.class.getSimpleName();
    private View rootView;
    private MyCameraManager myCameraManager;
    private PermissionManager permissionManager;
    private String mThermalImgPath;
    private boolean useDebugImg = false;
    private ImageView imageView_thermalViewFinder;
    private ThermalImage mThermalImage;
    private RelativeLayout relativeLayout_eyeNoseTemplate;
    private ImageView imageView_faceTemplate;
    private ProgressBar progressBar_loadingAnimation;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        relativeLayout_eyeNoseTemplate = findViewById(R.id.relativeLayout_eyeNoseTemplate);
        imageView_faceTemplate = findViewById(R.id.imageView_faceTemplate);
        progressBar_loadingAnimation = findViewById(R.id.progressBar_loadingAnimation);

        rootView = findViewById(R.id.linearLayout_CameraActivity);
        imageView_thermalViewFinder = findViewById(R.id.imageView_thermalViewFinder);
        myCameraManager = new MyCameraManager(getApplicationContext());
        permissionManager = new PermissionManager();

        //CheckforUsbDevice
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();

        CheckPermissions(deviceList);
        SetupCameraPreviewUi();
    }

    private void CheckPermissions(HashMap<String, UsbDevice> deviceList){
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
    }

    private void SetupCameraPreviewUi() {
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
                relativeLayout_eyeNoseTemplate.setVisibility(View.VISIBLE);
                imageView_faceTemplate.setVisibility(View.INVISIBLE);
                break;
        }
    }

    // TODO: Move to MarkerActivity
    private void ExecuteAlgorithm() {
        try {
            // Fix for Android Studio bug (returning to previous activity on "stop app")
            if (GlobalVariables.getCurrentAlgorithm() == null) {
                Snackbar.make(rootView, "Error - Algorithm not selected", Snackbar.LENGTH_SHORT).show();
                backOnClick(null);
            }

            GradientModel gradientAndPositions = null;

            switch (GlobalVariables.getCurrentAlgorithm()) {
                case CNN:
                    // Add execution for CNN
                    try {
                        ThermalImageFile thermalImageFile;
                        thermalImageFile = (ThermalImageFile) mThermalImage;
                        JavaImageBuffer javaBuffer = thermalImageFile.getImage();
                        Bitmap originalThermalImageBitmap = BitmapAndroid.createBitmap(javaBuffer).getBitMap();

                        String cnnModelFile = "RGB_yinguobingWideDens.tflite";
                        Cnn cnn = new Cnn(loadModelFile(this, cnnModelFile), thermalImageFile);
                        gradientAndPositions = cnn.getGradientAndPositions();
                        goToMarkerActivity(mThermalImgPath, gradientAndPositions);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case CNNWithTransferLearning:
                    // Add execution for CNN with transfer learning
                    break;
                case RgbThermalMapping:
                    detectFaces();
                    break;
                case MaxMinTemplate:
                    ImageView imageView_leftEye = findViewById(R.id.imageView_leftEye);
                    ImageView imageView_rightEye = findViewById(R.id.imageView_RightEye);
                    ImageView imageView_nose = findViewById(R.id.imageView_Nose);
                    View cameraPreviewElement = imageView_thermalViewFinder;
                    int[] leftEyeLocation = new int[2];
                    int[] rightEyeLocation = new int[2];
                    int[] noseLocation = new int[2];
                    int[] cameraPreviewLocation = new int[2];
                    imageView_leftEye.getLocationOnScreen(leftEyeLocation);
                    imageView_rightEye.getLocationOnScreen(rightEyeLocation);
                    imageView_nose.getLocationOnScreen(noseLocation);
                    cameraPreviewElement.getLocationOnScreen(cameraPreviewLocation);

                    MinMaxAlgorithm minMaxAlgorithm = new MinMaxAlgorithm(
                            mThermalImgPath,
                            new RoiModel(leftEyeLocation, imageView_leftEye.getHeight(), imageView_leftEye.getWidth()),
                            new RoiModel(rightEyeLocation, imageView_rightEye.getHeight(), imageView_rightEye.getWidth()),
                            new RoiModel(noseLocation, imageView_nose.getHeight(), imageView_nose.getWidth()),
                            new RoiModel(cameraPreviewLocation, cameraPreviewElement.getWidth(), cameraPreviewElement.getHeight())
                    );
                    gradientAndPositions = minMaxAlgorithm.getGradientAndPositions();
                    goToMarkerActivity(mThermalImgPath, gradientAndPositions);
                    break;
            }
        }
        // TODO: Un-comment following four catch clauses when algorithms have been added and throws appropriate custom exceptions
//        catch (CnnException e) {
//            Logging.error("ExecuteAlgorithm", e);
//        }
//        catch (CnnWithTransferLearningException e) {
//            Logging.error("ExecuteAlgorithm", e);
//        }
//        catch (RgbThermalMappingException e) {
//            Logging.error("ExecuteAlgorithm", e);
//        }
//        catch (MaxMinTemplateException e) {
//            Logging.error("ExecuteAlgorithm", e);
//        }
        catch (Exception e) {
            Logging.error("ExecuteAlgorithm", e);
        }
    }

    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void startView(HashMap<String, UsbDevice> deviceList) {
        if (!deviceList.isEmpty()) {
            Snackbar.make(rootView, "USB device is detected trying to connect", Snackbar.LENGTH_SHORT).show();
            flirCamera();
        }
        else {
            ThermalImageFile thermalImageFile = null;
            try {
                thermalImageFile = (ThermalImageFile) ImageFactory.createImage(getAssets().open("Thermal_Test_Img.jpg"));
                String fileName = "Thermal_Test_Img.jpg";
                mThermalImgPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Masterthesisimages/" + fileName;
                thermalImageFile.saveAs(mThermalImgPath);
                mThermalImage = thermalImageFile;
                JavaImageBuffer javaBuffer = thermalImageFile.getImage();
                imageView_thermalViewFinder.setImageBitmap(BitmapAndroid.createBitmap(javaBuffer).getBitMap());
                useDebugImg = true;
                Snackbar.make(rootView, "Cant find USB device opening phones camera using default img", Snackbar.LENGTH_SHORT).show();
            }
            catch (IOException e) {
                Snackbar.make(rootView, "an error accrued when open default img", Snackbar.LENGTH_SHORT).show();
                e.printStackTrace();
            }
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

    public void backOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void takePictureOnClick(View view) {
        Animation.showLoadingAnimation(progressBar_loadingAnimation, imageView_faceTemplate, relativeLayout_eyeNoseTemplate);
        takeAndSaveThermalImage();
    }

    private void takeAndSaveThermalImage() {

        if (useDebugImg) {
            ExecuteAlgorithm();
        }
        else {
            myCameraManager.addThermalImageListener((thermalImage) -> {
                File ImageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "/Masterthesisimages/");
                boolean isDirectoryCreated = ImageDir.exists() || ImageDir.mkdirs();
                try {
                    if (isDirectoryCreated) {
                        String fileName = new SimpleDateFormat("HH:mm:ss").format(new Timestamp(System.currentTimeMillis())) + "Thermal";
                        mThermalImgPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() + "/Masterthesisimages/" + fileName;
                        thermalImage.saveAs(mThermalImgPath);
                        mThermalImage = thermalImage;
                        ExecuteAlgorithm();
                    }
                    else {
                        Log.i(TAG, "takeAndSaveThermalImage: ERROR! IMAGE DIR NOT CREATED");
                        Animation.hideLoadingAnimation(progressBar_loadingAnimation, imageView_faceTemplate, relativeLayout_eyeNoseTemplate);
                        throw new IOException("Image Directory not created");
                    }
                }
                catch (IOException e) {
                    Log.d(TAG, "takeAndSaveThermalImage: ERROR: " + e);
                    Animation.hideLoadingAnimation(progressBar_loadingAnimation, imageView_faceTemplate, relativeLayout_eyeNoseTemplate);
                }
            });
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
        intent.putExtra("filename", imageFilePath);
        intent.putExtra("imageViewVerticalOffset", imageViewVerticalOffset);
        intent.putExtra("imageHeight", imageHeight);
        intent.putExtra("imageWidth", imageWidth);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gradientAndPositions", gradientAndPositions);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    // TODO: Move to RgbThermalAlgorithm class
    private void detectFaces() {
        Bitmap imageBitmap = null;
        int horizontalOffset = 50;
        int verticalOffset = 50;


        if (ThermalImageFile.isThermalImage(mThermalImgPath)) {
            try {
                ImageProcessing.FixImageOrientation(mThermalImgPath);
                ThermalImageFile thermalImageFile = (ThermalImageFile) ImageFactory.createImage(mThermalImgPath);
                JavaImageBuffer rgbImage = thermalImageFile.getFusion().getPhoto();
                imageBitmap = BitmapAndroid.createBitmap(rgbImage).getBitMap();
            }
            catch (IOException e) {
                e.printStackTrace();
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

                                            FirebaseVisionFaceLandmark leftEye = faces.get(0).getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
                                            if (leftEye != null) {
                                                gradientAndPositions.setEyePosition(new int[]{(int) ((float) leftEye.getPosition().getX()) + verticalOffset, (int) ((float) leftEye.getPosition().getY()) + horizontalOffset});
                                            }
                                            FirebaseVisionFaceLandmark nose = faces.get(0).getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE);
                                            if (nose != null) {
                                                gradientAndPositions.setNosePosition(new int[]{(int) ((float) nose.getPosition().getX()), (int) ((float) nose.getPosition().getY()) + horizontalOffset});
                                            }

                                            goToMarkerActivity(mThermalImgPath, gradientAndPositions);
                                        }
                                        else {
                                            Snackbar.make(rootView, "No faces found", Snackbar.LENGTH_SHORT).show();
                                            Animation.hideLoadingAnimation(progressBar_loadingAnimation, imageView_faceTemplate, relativeLayout_eyeNoseTemplate);
                                        }
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar.make(rootView, "Face detection error", Snackbar.LENGTH_SHORT).show();
                                        Animation.hideLoadingAnimation(progressBar_loadingAnimation, imageView_faceTemplate, relativeLayout_eyeNoseTemplate);
                                    }
                                });
    }
}
