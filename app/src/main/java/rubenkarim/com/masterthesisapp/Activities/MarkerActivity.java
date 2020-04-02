package rubenkarim.com.masterthesisapp.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Algorithms.CnnRectImg;
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
import rubenkarim.com.masterthesisapp.Utilities.Scaling;

public class MarkerActivity extends AppCompatActivity {

    //region Properties
    private String thermalImagePath;
    private int imageViewVerticalOffset;
    private int imageHeight;
    private int imageWidth;
    private ImageView imageView_thermalImageContainer;
    private RelativeLayout relativeLayout_markers;
    private GradientModel gradientAndPositions = null;
    private ThermalImageFile thermalImageFile;
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
            thermalImagePath = receivedIntent.getStringExtra("thermalImagePath");
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
            gradientAndPositions = (GradientModel) bundle.getSerializable("gradientAndPositions");
        }

        if (gradientAndPositions == null) {
            ExecuteAlgorithm();
        } else {
            setPicture(gradientAndPositions);
        }
    }

    private void ExecuteAlgorithm() {
        switch (GlobalVariables.getCurrentAlgorithm()) {
            case CNN:
                try {
                    String cnnModelFile = "RGB_yinguobingCNNV1.tflite";
                    CnnRectImg cnn = new CnnRectImg(this, cnnModelFile, thermalImagePath);
                    gradientAndPositions = cnn.getGradientAndPositions();
                    setPicture(gradientAndPositions);
                } catch (IOException e) {
                    Logging.error("ExecuteAlgorithm, CNN", e);
                    Snackbar.make(mRootView, "There was an error with the thermal image file try take a new picture", Snackbar.LENGTH_LONG);
                }
                break;
            case CNNWithTransferLearning:
                try {
                    String cnnTransferLearningModelFile = "RGB_InceptionV3.tflite";
                    CnnRectImg cnnTransferLearning = new CnnRectImg(this, cnnTransferLearningModelFile, thermalImagePath);
                    gradientAndPositions = cnnTransferLearning.getGradientAndPositions();
                    setPicture(gradientAndPositions);
                } catch (IOException e) {
                    Logging.error("ExecuteAlgorithm, CNNWithTransferLearning", e);
                    Snackbar.make(mRootView, "There was an error with the thermal image file try take a new picture", Snackbar.LENGTH_LONG);
                }
                break;
            case RgbThermalMapping:
                RgbThermalAlgorithm rgbThermalAlgorithm = new RgbThermalAlgorithm(this);
                rgbThermalAlgorithm.getGradientAndPositionsAsync(thermalImagePath, screenWidth, screenHeight);
                break;
            case MinMaxTemplate:
                MinMaxAlgorithm minMaxAlgorithm = new MinMaxAlgorithm(
                        thermalImagePath,
                        new RoiModel(minMaxData.getLeftEyeLocation(), minMaxData.getLeftEyeWidth(), minMaxData.getLeftEyeHeight()),
                        new RoiModel(minMaxData.getRightEyeLocation(), minMaxData.getRightEyeWidth(), minMaxData.getRightEyeHeight()),
                        new RoiModel(minMaxData.getNoseLocation(), minMaxData.getNoseWidth(), minMaxData.getNoseHeight()),
                        new RoiModel(minMaxData.getCameraPreviewContainerLocation(), minMaxData.getCameraPreviewContainerWidth(), minMaxData.getCameraPreviewContainerHeight())
                );
                setPicture(minMaxAlgorithm.getGradientAndPositions());
                break;
        }

    }

    public void setPicture(GradientModel gradientAndPositions) {
        try {
            this.gradientAndPositions = gradientAndPositions;
            ImageProcessing.FixImageOrientation(thermalImagePath);
            Bitmap originalThermalImageBitmap = ImageProcessing.convertToBitmap(thermalImagePath);

            imageView_thermalImageContainer.setImageBitmap(originalThermalImageBitmap);

            capturedImageDimensions = new int[]{originalThermalImageBitmap.getWidth(), originalThermalImageBitmap.getHeight()};
            imageContainerDimensions = new int[]{imageWidth, imageHeight};

            addMarkers(capturedImageDimensions, imageContainerDimensions, imageViewVerticalOffset);
            Animation.hideLoadingAnimation(progressBar_markerViewLoadingAnimation, null, null);
        } catch (IOException e) {
            Logging.error("setPicture: ", e);
            Snackbar.make(mRootView, "There was an error with the thermal image file try take a new picture", Snackbar.LENGTH_LONG);

        }
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
        int[] scaledEyeMarkerPosition = Scaling.upscaleCoordinatesFromImageToScreen(gradientAndPositions.getEyePosition(), capturedImageDimensions, imageContainerDimensions);
        eyeParams.leftMargin = scaledEyeMarkerPosition[0] - markerWidthHeight / 2;
        eyeParams.topMargin = scaledEyeMarkerPosition[1] - markerWidthHeight / 2;
        relativeLayout_markers.addView(imageView_eyeMarker, eyeParams);

        RelativeLayout.LayoutParams noseParams = new RelativeLayout.LayoutParams(markerWidthHeight, markerWidthHeight);
        int[] scaledNoseMarkerPosition = Scaling.upscaleCoordinatesFromImageToScreen(gradientAndPositions.getNosePosition(), capturedImageDimensions, imageContainerDimensions);
        noseParams.leftMargin = scaledNoseMarkerPosition[0] - markerWidthHeight / 2;
        noseParams.topMargin = scaledNoseMarkerPosition[1] - markerWidthHeight / 2;
        relativeLayout_markers.addView(imageView_noseMarker, noseParams);

        Log.e("addMarkers", "eye x: " + eyeParams.leftMargin + ", eye y: " + eyeParams.topMargin
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

        //getPixelColor(x, y);
    }

    private void getPixelColor(int x, int y) {
        Bitmap rootElementBitmap = ImageProcessing.loadBitmapFromView(imageView_thermalImageContainer);

        // Setting screen boundary programmatically
        x = x < 0 ? 0 : Math.min(x, imageWidth - 1);
        y = y < 0 ? 0 : Math.min(y, imageHeight - 1);

        int targetPixel = rootElementBitmap.getPixel(x, y);
        Log.e("Target pixel", "x: " + x + ", y: " + y);
        Log.e("Pixel color", Color.red(targetPixel) + "," + Color.green(targetPixel) + "," + Color.blue(targetPixel));
    }

    //region Navigation buttons
    public void backOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivity(intent);
    }

    public void submitOnClick(View view) {
        if (eyeAdjusted) {
            gradientAndPositions.setEyePosition(Scaling.downscaleCoordinatesFromScreenToImage(new int[]{adjustedEyePositionX, adjustedEyePositionY}, capturedImageDimensions, imageContainerDimensions));
        }
        if (noseAdjusted) {
            gradientAndPositions.setNosePosition(Scaling.downscaleCoordinatesFromScreenToImage(new int[]{adjustedNosePositionX, adjustedNosePositionY}, capturedImageDimensions, imageContainerDimensions));
        }

        if (eyeAdjusted || noseAdjusted) {
            recalculateGradient();
        }

        Bitmap thermalImageBitmapWithMarkers = ImageProcessing.convertThermalImageFileToBitmap(thermalImageFile);
        drawCircles(thermalImageBitmapWithMarkers, gradientAndPositions.getEyePosition(), gradientAndPositions.getNosePosition());

        Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
        intent.putExtra("thermalImagePath", thermalImagePath);
        intent.putExtra("imageHeight", imageHeight);
        intent.putExtra("imageWidth", imageWidth);
        intent.putExtra("imageViewVerticalOffset", imageViewVerticalOffset);
        intent.putExtra("thermalImageByteArrayWithMarkers", convertBitmapToByteArray(thermalImageBitmapWithMarkers));
        Bundle bundle = new Bundle();
        bundle.putSerializable("gradientAndPositions", gradientAndPositions);
        addMinMaxDataIfChosen(bundle);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void drawCircles(Bitmap bitmap, int[] eye, int[] nose) {
        for (int i = 0; i < 10; i++) {
            bitmap.setPixel(eye[0] + i, eye[1], Color.RED);
            bitmap.setPixel(eye[0] - i, eye[1], Color.RED);
            bitmap.setPixel(eye[0], eye[1] + i, Color.RED);
            bitmap.setPixel(eye[0], eye[1] - i, Color.RED);
            bitmap.setPixel(nose[0] + i, nose[1], Color.RED);
            bitmap.setPixel(nose[0] - i, nose[1], Color.RED);
            bitmap.setPixel(nose[0], nose[1] + i, Color.RED);
            bitmap.setPixel(nose[0], nose[1] - i, Color.RED);
        }
    }

    private byte[] convertBitmapToByteArray(Bitmap thermalImageBitmapWithDots) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thermalImageBitmapWithDots.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private void recalculateGradient() {
        thermalImageFile.getMeasurements().addSpot(gradientAndPositions.getEyePosition()[0], gradientAndPositions.getEyePosition()[1]);
        thermalImageFile.getMeasurements().addSpot(gradientAndPositions.getNosePosition()[0], gradientAndPositions.getNosePosition()[1]);
        double eye = thermalImageFile.getMeasurements().getSpots().get(0).getValue().value;
        double nose = thermalImageFile.getMeasurements().getSpots().get(1).getValue().value;
        gradientAndPositions.setGradient(eye - nose);
    }

    private void addMinMaxDataIfChosen(Bundle bundle) {
        if (GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.MinMaxTemplate) {
            bundle.putSerializable("minMaxData", minMaxData);
        }
    }
    //endregion
}