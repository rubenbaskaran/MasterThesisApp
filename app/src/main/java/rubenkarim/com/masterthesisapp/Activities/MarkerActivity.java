package rubenkarim.com.masterthesisapp.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.flir.thermalsdk.image.ImageFactory;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.fusion.FusionMode;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Algorithms.AlgorithmResult;
import rubenkarim.com.masterthesisapp.Algorithms.CnnAlgorithm;
import rubenkarim.com.masterthesisapp.Algorithms.MinMaxAlgorithm;
import rubenkarim.com.masterthesisapp.Algorithms.RgbThermalAlgorithm;
import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.Models.RoiModel;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.Animation;
import rubenkarim.com.masterthesisapp.Utilities.GlobalVariables;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;
import rubenkarim.com.masterthesisapp.Utilities.Logging;
import rubenkarim.com.masterthesisapp.Utilities.MinMaxDataTransferContainer;
import rubenkarim.com.masterthesisapp.Utilities.NeuralNetworkLoader;
import rubenkarim.com.masterthesisapp.Utilities.Scaling;

public class MarkerActivity extends AppCompatActivity implements AlgorithmResult {

    //region Properties
    private static final String TAG = MarkerActivity.class.getSimpleName();
    private int imageViewVerticalOffset;
    private int imageHeight;
    private int imageWidth;
    private ImageView imageView_thermalImageContainer;
    private RelativeLayout relativeLayout_markers;
    private GradientModel mGradientAndPositions = null;
    private ThermalImageFile mThermalImage;
    private ProgressBar progressBar_markerViewLoadingAnimation;
    private MinMaxDataTransferContainer minMaxData;
    private int[] capturedImageDimensions;
    private int[] imageContainerDimensions;
    private int adjustedNosePositionX;
    private int adjustedNosePositionY;
    private int adjustedEyePositionX;
    private int adjustedEyePositionY;
    private boolean eyeAdjusted = false;
    private boolean noseAdjusted = false;
    private int screenWidth;
    private int screenHeight;
    private View mRootView;
    private String mThermalImagePath;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        imageView_thermalImageContainer = findViewById(R.id.imageView_thermalImageContainer);
        relativeLayout_markers = findViewById(R.id.relativeLayout_thermalImageContainer);
        progressBar_markerViewLoadingAnimation = findViewById(R.id.progressBar_markerViewLoadingAnimation);
        mRootView = findViewById(R.id.linearLayout_MarkerActivity);

        Animation.showLoadingAnimation(progressBar_markerViewLoadingAnimation, null, null);

        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra("thermalImagePath")) {
            mThermalImagePath = receivedIntent.getStringExtra("thermalImagePath");
            try {
                ImageProcessing.FixImageOrientation(mThermalImagePath);
                mThermalImage = (ThermalImageFile) ImageFactory.createImage(mThermalImagePath);
                mThermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
            } catch (IOException e) {
                Logging.error(this,TAG + " onCreate: ", e);
                Snackbar.make(mRootView, R.string.errorThermal_Img, Snackbar.LENGTH_INDEFINITE).show();
            }
        }
        if (receivedIntent.hasExtra("imageViewVerticalOffset")) {
            imageViewVerticalOffset = receivedIntent.getIntExtra("imageViewVerticalOffset", 0);
        }
        if (receivedIntent.hasExtra("imageHeight")) {
            imageHeight = receivedIntent.getIntExtra("imageHeight", 0);
        }
        if (receivedIntent.hasExtra("imageWidth")) {
            imageWidth = receivedIntent.getIntExtra("imageWidth", 0);
        }
        if (receivedIntent.hasExtra("screenWidth")) {
            screenWidth = receivedIntent.getIntExtra("screenWidth", 0);
        }
        if (receivedIntent.hasExtra("screenHeight")) {
            screenHeight = receivedIntent.getIntExtra("screenHeight", 0);
        }

        Bundle bundle = receivedIntent.getExtras();
        if (bundle != null) {
            minMaxData = (MinMaxDataTransferContainer) bundle.getSerializable("minMaxData");
            mGradientAndPositions = (GradientModel) bundle.getSerializable("gradientAndPositions");
        }

        if (mGradientAndPositions == null) {
            ExecuteAlgorithm();
        } else {
            setPicture(mGradientAndPositions);
        }
    }

    private void ExecuteAlgorithm() {
        switch (GlobalVariables.getCurrentAlgorithm()) {
            case CNN:
                    new Thread(()->{
                        try {
                            CnnAlgorithm cnn = new CnnAlgorithm(NeuralNetworkLoader.loadCnn(this), mThermalImage);
                            cnn.getGradientAndPositions(this);
                        } catch (IOException e) {
                            Logging.error(this,"ExecuteAlgorithm(), CNN", e);
                            Snackbar.make(mRootView, R.string.errorAlgorithm, Snackbar.LENGTH_LONG).show();
                        }
                    }).start();
                break;

            case CNNWithTransferLearning:
                new Thread(()->{
                    try {
                        CnnAlgorithm cnnTransferLearning = new CnnAlgorithm(NeuralNetworkLoader.loadCnnTransferLearning(this), mThermalImage);
                        cnnTransferLearning.getGradientAndPositions(this);
                    } catch (IOException e) {
                        Logging.error(this,"ExecuteAlgorithm(), CNNWithTransferLearning", e);
                        Snackbar.make(mRootView, R.string.errorAlgorithm, Snackbar.LENGTH_LONG).show();
                    }
                }).start();
                break;

            case RgbThermalMapping:
                new Thread(()->{
                    Logging.info(this,TAG, "starting RGB");
                    RgbThermalAlgorithm rgbThermalAlgorithm = new RgbThermalAlgorithm(mThermalImage);
                    rgbThermalAlgorithm.getGradientAndPositions(this, screenWidth, screenHeight);
                }).start();
                break;

            case MinMaxTemplate:
                new Thread(()->{
                    MinMaxAlgorithm minMaxAlgorithm = new MinMaxAlgorithm(
                            mThermalImage,
                            new RoiModel(minMaxData.getLeftEyeLocation(), minMaxData.getLeftEyeWidth(), minMaxData.getLeftEyeHeight()),
                            new RoiModel(minMaxData.getRightEyeLocation(), minMaxData.getRightEyeWidth(), minMaxData.getRightEyeHeight()),
                            new RoiModel(minMaxData.getNoseLocation(), minMaxData.getNoseWidth(), minMaxData.getNoseHeight()),
                            new RoiModel(minMaxData.getCameraPreviewContainerLocation(), minMaxData.getCameraPreviewContainerWidth(), minMaxData.getCameraPreviewContainerHeight())
                    );
                    minMaxAlgorithm.getGradientAndPositions(this);
                }).start();
                break;
        }

    }

    @Override
    public void onResult(GradientModel gradientModel) {
        runOnUiThread(() -> {
            this.setPicture(gradientModel);
        });
    }

    @Override
    public void onError(String errorMessage) {
        Logging.info(this, TAG,"Algorithm: " + errorMessage);
        runOnUiThread(() -> {
            Snackbar.make(mRootView, errorMessage, Snackbar.LENGTH_LONG).show();
        });
    }

    public void setPicture(GradientModel gradientAndPositions) {
            this.mGradientAndPositions = gradientAndPositions;

            mThermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
            Bitmap thermalImgBitmap = ImageProcessing.getBitmap(mThermalImage);
            imageView_thermalImageContainer.setImageBitmap(thermalImgBitmap);

            capturedImageDimensions = new int[]{thermalImgBitmap.getWidth(), thermalImgBitmap.getHeight()};
            imageContainerDimensions = new int[]{imageWidth, imageHeight};

            addMarkers(capturedImageDimensions, imageContainerDimensions, imageViewVerticalOffset);
            Animation.hideLoadingAnimation(progressBar_markerViewLoadingAnimation, null, null);
    }

    private void addMarkers(int[] capturedImageDimensions, int[] imageContainerDimensions, int horizontalOffset) {
        ImageView imageView_eyeMarker = new ImageView(this);
        ImageView imageView_noseMarker = new ImageView(this);

        imageView_eyeMarker.setTag("eye");
        imageView_noseMarker.setTag("nose");

        imageView_eyeMarker.setImageURI(Uri.parse("android.resource://" + this.getPackageName() + "/drawable/eye_marker"));
        imageView_noseMarker.setImageURI(Uri.parse("android.resource://" + this.getPackageName() + "/drawable/nose_marker"));

        SetOnTouchListener(imageView_noseMarker);
        SetOnTouchListener(imageView_eyeMarker);
        int markerWidthHeight = ((BitmapDrawable) imageView_eyeMarker.getDrawable()).getBitmap().getWidth();

        RelativeLayout.LayoutParams eyeParams = new RelativeLayout.LayoutParams(markerWidthHeight, markerWidthHeight);
        int[] scaledEyeMarkerPosition = Scaling.upscaleCoordinatesFromImageToScreen(mGradientAndPositions.getEyePosition(), capturedImageDimensions, imageContainerDimensions);
        eyeParams.leftMargin = scaledEyeMarkerPosition[0] - markerWidthHeight / 2;
        eyeParams.topMargin = scaledEyeMarkerPosition[1] - markerWidthHeight / 2;
        relativeLayout_markers.addView(imageView_eyeMarker, eyeParams);

        RelativeLayout.LayoutParams noseParams = new RelativeLayout.LayoutParams(markerWidthHeight, markerWidthHeight);
        int[] scaledNoseMarkerPosition = Scaling.upscaleCoordinatesFromImageToScreen(mGradientAndPositions.getNosePosition(), capturedImageDimensions, imageContainerDimensions);
        noseParams.leftMargin = scaledNoseMarkerPosition[0] - markerWidthHeight / 2;
        noseParams.topMargin = scaledNoseMarkerPosition[1] - markerWidthHeight / 2;
        relativeLayout_markers.addView(imageView_noseMarker, noseParams);

        Log.d("addMarkers", "eye x: " + eyeParams.leftMargin + ", eye y: " + eyeParams.topMargin
                + ". nose x: " + noseParams.leftMargin + ", nose y: " + noseParams.topMargin
                + ". imageView x: " + imageWidth + ", imageView y: " + imageHeight
                + ". markerWidth: " + markerWidthHeight);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void SetOnTouchListener(ImageView imageView) {
        imageView.setOnTouchListener(new View.OnTouchListener() {
            PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
            PointF StartPT = new PointF(); // Record Start Position of 'imageView'

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        if ((StartPT.x + event.getX() - DownPT.x) < 0 || (StartPT.y + event.getY() - DownPT.y) < 0) {
                            break;
                        }
                        if ((StartPT.x + event.getX() - DownPT.x) > imageWidth - imageView.getWidth() || (StartPT.y + event.getY() - DownPT.y) > imageHeight - imageView.getHeight()) {
                            break;
                        }
                        imageView.setX((int) (StartPT.x + event.getX() - DownPT.x));
                        imageView.setY((int) (StartPT.y + event.getY() - DownPT.y));
                        StartPT.set(imageView.getX(), imageView.getY());
                        break;
                    case MotionEvent.ACTION_DOWN:
                        DownPT.set(event.getX(), event.getY());
                        StartPT.set(imageView.getX(), imageView.getY());
                        break;
                    case MotionEvent.ACTION_UP:
                        getCoordinates(imageView);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void getCoordinates(ImageView marker) {
        Bitmap markerBitmap = ((BitmapDrawable) marker.getDrawable()).getBitmap();
        int markerVerticalOffset = markerBitmap.getHeight();
        int markerHorizontalOffset = markerBitmap.getWidth();
        int[] coordinates = new int[2];
        marker.getLocationOnScreen(coordinates);

        int x = coordinates[0] + markerHorizontalOffset / 2;
        int y = coordinates[1] + markerVerticalOffset / 2 - imageViewVerticalOffset;

        if (marker.getTag() == "eye") {
            adjustedEyePositionX = x;
            adjustedEyePositionY = y;
            eyeAdjusted = true;
        } else if (marker.getTag() == "nose") {
            adjustedNosePositionX = x;
            adjustedNosePositionY = y;
            noseAdjusted = true;
        }
        Log.e(String.valueOf(marker.getTag()), "x: " + x + ", y: " + y);

    }



    //region Navigation buttons
    public void backOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivity(intent);
    }

    public void submitOnClick(View view) {
        if (eyeAdjusted) {
            mGradientAndPositions.setEyePosition(Scaling.downscaleCoordinatesFromScreenToImage(new int[]{adjustedEyePositionX, adjustedEyePositionY}, capturedImageDimensions, imageContainerDimensions));
        }
        if (noseAdjusted) {
            mGradientAndPositions.setNosePosition(Scaling.downscaleCoordinatesFromScreenToImage(new int[]{adjustedNosePositionX, adjustedNosePositionY}, capturedImageDimensions, imageContainerDimensions));
        }

        if (eyeAdjusted || noseAdjusted) {
            recalculateGradient(mThermalImage);
            mGradientAndPositions.setMarkersAdjusted(true);
        }

        Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
        intent.putExtra("thermalImagePath", mThermalImagePath);
        intent.putExtra("imageHeight", imageHeight);
        intent.putExtra("imageWidth", imageWidth);
        intent.putExtra("imageViewVerticalOffset", imageViewVerticalOffset);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gradientAndPositions", mGradientAndPositions);
        addMinMaxDataIfChosen(bundle);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void recalculateGradient(ThermalImageFile thermalImageFile) {
        thermalImageFile.getMeasurements().addSpot(mGradientAndPositions.getEyePosition()[0], mGradientAndPositions.getEyePosition()[1]);
        thermalImageFile.getMeasurements().addSpot(mGradientAndPositions.getNosePosition()[0], mGradientAndPositions.getNosePosition()[1]);
        double eye = thermalImageFile.getMeasurements().getSpots().get(0).getValue().value;
        double nose = thermalImageFile.getMeasurements().getSpots().get(1).getValue().value;
        mGradientAndPositions.setGradient(eye - nose);
    }

    private void addMinMaxDataIfChosen(Bundle bundle) {
        if (GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.MinMaxTemplate) {
            bundle.putSerializable("minMaxData", minMaxData);
        }
    }
    //endregion
}