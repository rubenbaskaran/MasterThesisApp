package rubenkarim.com.masterthesisapp.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.flir.thermalsdk.image.ImageFactory;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.fusion.FusionMode;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Algorithms.AbstractAlgorithmTask;
import rubenkarim.com.masterthesisapp.Algorithms.AlgorithmResultListener;
import rubenkarim.com.masterthesisapp.Algorithms.RgbThermalAlgorithmTask;
import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.Animation;
import rubenkarim.com.masterthesisapp.Utilities.GlobalVariables;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;
import rubenkarim.com.masterthesisapp.Utilities.Logging;
import rubenkarim.com.masterthesisapp.Utilities.NoFaceDetectedException;
import rubenkarim.com.masterthesisapp.Utilities.Scaling;

public class MarkerActivity extends AppCompatActivity implements AlgorithmResultListener {

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
    private Button button_Submit;
    private Button button_Back;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        imageView_thermalImageContainer = findViewById(R.id.imageView_thermalImageContainer);
        relativeLayout_markers = findViewById(R.id.relativeLayout_thermalImageContainer);
        progressBar_markerViewLoadingAnimation = findViewById(R.id.progressBar_markerViewLoadingAnimation);
        mRootView = findViewById(R.id.linearLayout_MarkerActivity);
        button_Submit = findViewById(R.id.button_Submit);
        button_Back = findViewById(R.id.button_Back);

        button_Back.setEnabled(false);
        button_Submit.setEnabled(false);
        Animation.showLoadingAnimation(progressBar_markerViewLoadingAnimation, null);

        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra("thermalImagePath")) {
            mThermalImagePath = receivedIntent.getStringExtra("thermalImagePath");
            try {
                ImageProcessing.FixImageOrientation(mThermalImagePath);
                mThermalImage = (ThermalImageFile) ImageFactory.createImage(mThermalImagePath);
                mThermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
            } catch (IOException e) {
                Logging.error(this, TAG + " onCreate: ", e);
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
            mGradientAndPositions = (GradientModel) bundle.getSerializable("gradientAndPositions");
        }
        if (mThermalImage != null) {
            if (mGradientAndPositions == null) {
                executeAlgorithm();
            } else {
                setPicture(mGradientAndPositions);
            }
        } else {
            Snackbar.make(mRootView, R.string.errorThermal_Img, Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    private void executeAlgorithm() {
        switch (GlobalVariables.getCurrentAlgorithm()) {
            case RgbThermalMapping:
                new Thread(() -> {
                    Logging.info(this, TAG, "starting RGB");
                    AbstractAlgorithmTask rgbThermalAlgorithm = new RgbThermalAlgorithmTask(mThermalImage, screenWidth, screenHeight);
                    rgbThermalAlgorithm.getGradientAndPositions(this);
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
    public void onError(Exception exception) {
        if (exception instanceof NoFaceDetectedException){
            Logging.info(this, TAG, "Algorithm onError: " + exception);
            runOnUiThread(() -> {
                Snackbar.make(mRootView, exception.getMessage(), Snackbar.LENGTH_LONG).show();
                setPicture(new GradientModel(0.0, new int[]{100, 100}, new int[]{100,200}, 0, 0));
            });
        }

    }

    public void setPicture(GradientModel gradientModel) {
        this.mGradientAndPositions = gradientModel;

        mThermalImage.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
        Bitmap thermalImgBitmap = ImageProcessing.getBitmap(mThermalImage);
        imageView_thermalImageContainer.setImageBitmap(thermalImgBitmap);

        capturedImageDimensions = new int[]{thermalImgBitmap.getWidth(), thermalImgBitmap.getHeight()};
        imageContainerDimensions = new int[]{imageWidth, imageHeight};
        addMarkers(capturedImageDimensions, imageContainerDimensions);
        Animation.hideLoadingAnimation(progressBar_markerViewLoadingAnimation, null);
        button_Submit.setEnabled(true);
        button_Back.setEnabled(true);
    }

    private void addMarkers(int[] capturedImageDimensions, int[] imageContainerDimensions) {
        ImageView imageView_eyeMarker = new ImageView(this);
        ImageView imageView_noseMarker = new ImageView(this);

        imageView_eyeMarker.setTag("eye");
        imageView_noseMarker.setTag("nose");

        imageView_eyeMarker.setImageURI(Uri.parse("android.resource://" + this.getPackageName() + "/drawable/eye_marker_large"));
        imageView_noseMarker.setImageURI(Uri.parse("android.resource://" + this.getPackageName() + "/drawable/nose_marker_large"));

        setOnTouchListener(imageView_noseMarker);
        setOnTouchListener(imageView_eyeMarker);
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
    private void setOnTouchListener(ImageView imageView) {
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

    private void recalculateGradient(ThermalImageFile thermalImageFile) {
        double eye = AbstractAlgorithmTask.calculateTemperature(mGradientAndPositions.getEyePosition()[0], mGradientAndPositions.getEyePosition()[1], thermalImageFile);
        double nose = AbstractAlgorithmTask.calculateTemperature(mGradientAndPositions.getNosePosition()[0], mGradientAndPositions.getNosePosition()[1], thermalImageFile);
        mGradientAndPositions.setEyeTemperature(eye);
        mGradientAndPositions.setNoseTemperature(nose);
        mGradientAndPositions.setGradient(Math.abs(eye - nose));
    }

    //region Navigation buttons
    public void backOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivity(intent);
    }

    public void submitOnClick(View view) {
        if (eyeAdjusted) {
            mGradientAndPositions.setEyePosition(Scaling.downscaleCoordinatesFromScreenToImage(new int[]{adjustedEyePositionX, adjustedEyePositionY}, capturedImageDimensions, imageContainerDimensions));
            mGradientAndPositions.setEyeMarkerAdjusted(true);
        }
        if (noseAdjusted) {
            mGradientAndPositions.setNosePosition(Scaling.downscaleCoordinatesFromScreenToImage(new int[]{adjustedNosePositionX, adjustedNosePositionY}, capturedImageDimensions, imageContainerDimensions));
            mGradientAndPositions.setNoseMarkerAdjusted(true);
        }

        if (eyeAdjusted || noseAdjusted) {
            recalculateGradient(mThermalImage);
        }

        Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
        intent.putExtra("thermalImagePath", mThermalImagePath);
        intent.putExtra("imageHeight", imageHeight);
        intent.putExtra("imageWidth", imageWidth);
        intent.putExtra("imageViewVerticalOffset", imageViewVerticalOffset);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gradientAndPositions", mGradientAndPositions);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    //endregion
}