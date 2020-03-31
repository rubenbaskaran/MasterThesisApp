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

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Algorithms.Cnn;
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
    private GradientModel gradientAndPositions;
    private ThermalImageFile thermalImageFile;
    private ProgressBar progressBar_markerViewLoadingAnimation;
    private MinMaxDataTransferContainer minMaxData;
    private int[] capturedImageDimensions;
    private int[] imageContainerDimensions;
    private int adjustedNosePositionX;
    private int adjustedNosePositionY;
    private int adjustedEyePositionX;
    private int adjustedEyePositionY;
    private boolean eyeAdjusted;
    private boolean noseAdjusted;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_marker);
            imageView_thermalImageContainer = findViewById(R.id.imageView_thermalImageContainer);
            progressBar_markerViewLoadingAnimation = findViewById(R.id.progressBar_markerViewLoadingAnimation);

            Animation.showLoadingAnimation(progressBar_markerViewLoadingAnimation, null, null);

            Intent receivedIntent = getIntent();
            if (receivedIntent.hasExtra("thermalImagePath")) {
                thermalImagePath = receivedIntent.getStringExtra("thermalImagePath");
                thermalImageFile = (ThermalImageFile) ImageFactory.createImage(thermalImagePath);
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

            Bundle bundle = receivedIntent.getExtras();
            if (bundle != null) {
                minMaxData = (MinMaxDataTransferContainer) bundle.getSerializable("minMaxData");
            }

            ExecuteAlgorithm();
        }
        catch (Exception e) {
            Logging.error("onCreate", e);
        }
    }

    private void ExecuteAlgorithm() {
        try {
            GradientModel gradientAndPositions;

            switch (GlobalVariables.getCurrentAlgorithm()) {
                case CNN:
                    String cnnModelFile = "RGB_yinguobingWideDens.tflite";
                    Cnn cnn = new Cnn(this, cnnModelFile, thermalImageFile);
                    gradientAndPositions = cnn.getGradientAndPositions();
                    setPicture(gradientAndPositions);
                    break;
                case CNNWithTransferLearning:
                    // Add execution for CNN with transfer learning
                    break;
                case RgbThermalMapping:
                    RgbThermalAlgorithm rgbThermalAlgorithm = new RgbThermalAlgorithm(this);
                    rgbThermalAlgorithm.getGradientAndPositionsAsync(thermalImagePath);
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
        }
        catch (Exception e) {
            Logging.error("setPicture", e);
            Animation.hideLoadingAnimation(progressBar_markerViewLoadingAnimation, null, null);
        }
    }

    private void addMarkers(int[] capturedImageDimensions, int[] imageContainerDimensions, int horizontalOffset) {
        try {
            int markerWidthHeight = 200;

            RelativeLayout relativeLayout_markers = findViewById(R.id.relativeLayout_thermalImageContainer);
            ImageView imageView_eyeMarker = new ImageView(this);
            ImageView imageView_noseMarker = new ImageView(this);

            imageView_eyeMarker.setTag("eye");
            imageView_noseMarker.setTag("nose");

            imageView_eyeMarker.setImageURI(Uri.parse("android.resource://" + this.getPackageName() + "/drawable/eye_marker"));
            imageView_noseMarker.setImageURI(Uri.parse("android.resource://" + this.getPackageName() + "/drawable/nose_marker"));

            SetOnTouchListener(imageView_noseMarker);
            SetOnTouchListener(imageView_eyeMarker);

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
        catch (Exception e) {
            Logging.error("addMarkers", e);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void SetOnTouchListener(ImageView imageView) {
        try {
            imageView.setOnTouchListener(new View.OnTouchListener() {
                PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
                PointF StartPT = new PointF(); // Record Start Position of 'imageView'

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
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
        catch (Exception e) {
            Logging.error("SetOnTouchListener", e);
        }
    }

    private void getCoordinates(ImageView marker) {
        try {
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
            }
            else if (marker.getTag() == "nose") {
                adjustedNosePositionX = x;
                adjustedNosePositionY = y;
                noseAdjusted = true;
            }
            Log.e(String.valueOf(marker.getTag()), "x: " + x + ", y: " + y);

            //getPixelColor(x, y);
        }
        catch (Exception e) {
            Logging.error("getCoordinates", e);
        }
    }

    private void getPixelColor(int x, int y) {
        try {
            // TODO: Test marker precision on color_test.png
            Bitmap rootElementBitmap = ImageProcessing.loadBitmapFromView(imageView_thermalImageContainer);

            // Setting screen boundary programmatically
            x = x < 0 ? 0 : Math.min(x, imageWidth - 1);
            y = y < 0 ? 0 : Math.min(y, imageHeight - 1);

            int targetPixel = rootElementBitmap.getPixel(x, y);
            Log.e("Target pixel", "x: " + x + ", y: " + y);
            Log.e("Pixel color", Color.red(targetPixel) + "," + Color.green(targetPixel) + "," + Color.blue(targetPixel));
        }
        catch (Exception e) {
            Logging.error("getPixelColor", e);
        }
    }

    //region Navigation buttons
    public void backOnClick(View view) {
        try {
            Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
            startActivity(intent);
        }
        catch (Exception e) {
            Logging.error("backOnClick", e);
        }
    }

    public void submitOnClick(View view) {
        try {
            if (eyeAdjusted) {
                gradientAndPositions.setEyePosition(Scaling.downscaleCoordinatesFromScreenToImage(new int[]{adjustedEyePositionX, adjustedEyePositionY}, capturedImageDimensions, imageContainerDimensions));
            }
            if (noseAdjusted) {
                gradientAndPositions.setNosePosition(Scaling.downscaleCoordinatesFromScreenToImage(new int[]{adjustedNosePositionX, adjustedNosePositionY}, capturedImageDimensions, imageContainerDimensions));
            }

            // TODO: Calculate new gradient based on adjusted value for eye/nose and save in gradientAndPositions object

            Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
            intent.putExtra("thermalImagePath", thermalImagePath);
            intent.putExtra("imageHeight", imageHeight);
            intent.putExtra("imageWidth", imageWidth);
            Bundle bundle = new Bundle();
            bundle.putSerializable("gradientAndPositions", gradientAndPositions);
            addMinMaxDataIfChosen(bundle);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        catch (Exception e) {
            Logging.error("submitOnClick", e);
        }
    }

    private void addMinMaxDataIfChosen(Bundle bundle) {
        if (GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.MinMaxTemplate) {
            bundle.putSerializable("minMaxData", minMaxData);
        }
    }
    //endregion
}