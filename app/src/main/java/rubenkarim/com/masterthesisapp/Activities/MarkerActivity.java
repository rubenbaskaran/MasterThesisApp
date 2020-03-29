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
import android.widget.RelativeLayout;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.ImageFactory;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.fusion.FusionMode;
import com.google.android.material.snackbar.Snackbar;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Algorithms.Cnn;
import rubenkarim.com.masterthesisapp.Algorithms.MinMaxAlgorithm;
import rubenkarim.com.masterthesisapp.Algorithms.RgbThermalAlgorithm;
import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.Models.RoiModel;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.GlobalVariables;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;
import rubenkarim.com.masterthesisapp.Utilities.Logging;
import rubenkarim.com.masterthesisapp.Utilities.Scaling;

public class MarkerActivity extends AppCompatActivity {

    //region Properties
    private static final String TAG = MarkerActivity.class.getSimpleName();
    private String filepath;
    private int imageViewVerticalOffset;
    private int imageHeight;
    private int imageWidth;
    private ImageView imageView_markerImage;
    private GradientModel gradientAndPositions;
    private ThermalImageFile mThermalImage;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_marker);
            imageView_markerImage = findViewById(R.id.imageView_markerImage);

            Intent receivedIntent = getIntent();
            if (receivedIntent.hasExtra("filepath")) {
                filepath = receivedIntent.getStringExtra("filepath");
                assert filepath != null;
                mThermalImage = (ThermalImageFile) ImageFactory.createImage(filepath);

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
                gradientAndPositions = (GradientModel) bundle.getSerializable("gradientAndPositions");
            }

            ExecuteAlgorithm();
        } catch (Exception e) {
            Logging.error("onCreate", e);
            //throw e; //TODO: Dont throw in onCreating the app will crash! Handle the error instead
        }
    }

    private void ExecuteAlgorithm() {
        try {
            // Fix for Android Studio bug (returning to previous activity on "stop app")
            if (GlobalVariables.getCurrentAlgorithm() == null) {
                Snackbar.make(findViewById(R.id.linearLayout_MarkerActivity), "Error - Algorithm not selected", Snackbar.LENGTH_SHORT).show();
                backOnClick(null);
            }

            GradientModel gradientAndPositions = null;

            switch (GlobalVariables.getCurrentAlgorithm()) {
                case CNN:
                    String cnnModelFile = "RGB_yinguobingWideDens.tflite";
                    Cnn cnn = new Cnn(this, cnnModelFile, mThermalImage);
                    gradientAndPositions = cnn.getGradientAndPositions();
                    setPicture(gradientAndPositions);
                    break;
                case CNNWithTransferLearning:
                    // Add execution for CNN with transfer learning
                    break;
                case RgbThermalMapping:
                    RgbThermalAlgorithm rgbThermalAlgorithm = new RgbThermalAlgorithm(this);
                    rgbThermalAlgorithm.getGradientAndPositions(filepath);
                    break;
                case MaxMinTemplate:
                    ImageView imageView_leftEye = findViewById(R.id.imageView_leftEye);
                    ImageView imageView_rightEye = findViewById(R.id.imageView_RightEye);
                    ImageView imageView_nose = findViewById(R.id.imageView_Nose);
                    RelativeLayout cameraPreviewElement = findViewById(R.id.relativeLayout_markers);
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

            ImageProcessing.FixImageOrientation(filepath);
            int[] imageOriginalDimensions = null;

            if (ThermalImageFile.isThermalImage(filepath)) {
                ThermalImageFile thermalImageFile = (ThermalImageFile) ImageFactory.createImage(filepath);
                thermalImageFile.getFusion().setFusionMode(FusionMode.THERMAL_ONLY); //Is showing only Thermal picture wit resolution of 480x640
                JavaImageBuffer javaBuffer = thermalImageFile.getImage();
                Bitmap originalThermalImageBitmap = BitmapAndroid.createBitmap(javaBuffer).getBitMap();
                imageView_markerImage.setImageBitmap(originalThermalImageBitmap);
                imageOriginalDimensions = new int[]{originalThermalImageBitmap.getWidth(), originalThermalImageBitmap.getHeight()};
            } else {
                Log.e("SetPicture", "IS NOT A THERMAL PICTURE");
            }

            addMarkers(imageOriginalDimensions, new int[]{imageWidth, imageHeight}, imageViewVerticalOffset);
        } catch (Exception e) {
            Logging.error("setPicture", e);
        }
    }

    private void addMarkers(int[] imageOriginalDimensions, int[] imageViewDimensions, int horizontalOffset) {
        try {
            int markerWidthHeight = 200;

            RelativeLayout relativeLayout_markers = findViewById(R.id.relativeLayout_markers);
            ImageView imageView_eyeMarker = new ImageView(this);
            ImageView imageView_noseMarker = new ImageView(this);

            imageView_eyeMarker.setImageURI(Uri.parse("android.resource://" + this.getPackageName() + "/drawable/eye_marker"));
            imageView_noseMarker.setImageURI(Uri.parse("android.resource://" + this.getPackageName() + "/drawable/nose_marker"));

            SetOnTouchListener(imageView_noseMarker);
            SetOnTouchListener(imageView_eyeMarker);

            RelativeLayout.LayoutParams eyeParams = new RelativeLayout.LayoutParams(markerWidthHeight, markerWidthHeight);
            int[] scaledEyeMarkerPosition = Scaling.getScaledMarkerPosition(gradientAndPositions.getEyePosition(), imageOriginalDimensions, imageViewDimensions, horizontalOffset);
            eyeParams.leftMargin = scaledEyeMarkerPosition[0] - markerWidthHeight / 2;
            eyeParams.topMargin = scaledEyeMarkerPosition[1] - markerWidthHeight / 2;
            relativeLayout_markers.addView(imageView_eyeMarker, eyeParams);

            RelativeLayout.LayoutParams noseParams = new RelativeLayout.LayoutParams(markerWidthHeight, markerWidthHeight);
            int[] scaledNoseMarkerPosition = Scaling.getScaledMarkerPosition(gradientAndPositions.getNosePosition(), imageOriginalDimensions, imageViewDimensions, horizontalOffset);
            noseParams.leftMargin = scaledNoseMarkerPosition[0] - markerWidthHeight / 2;
            noseParams.topMargin = scaledNoseMarkerPosition[1] - markerWidthHeight / 2;
            relativeLayout_markers.addView(imageView_noseMarker, noseParams);

            Log.e("addMarkers", "eye x: " + eyeParams.leftMargin + ", eye y: " + eyeParams.topMargin
                    + ". nose x: " + noseParams.leftMargin + ", nose y: " + noseParams.topMargin
                    + ". imageView x: " + imageWidth + ", imageView y: " + imageHeight
                    + ". markerWidth: " + markerWidthHeight + ". horizontal offset: " + horizontalOffset);
        } catch (Exception e) {
            Logging.error("addMarkers", e);
            throw e;
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
            // TODO: Save new coordinate for eye/nose in gradientAndPositions object (remember to scale to original image dimensions before saving)
            Log.e(String.valueOf(marker.getTag()), "x: " + x + ", y: " + y);
            getPixelColor(x, y);
        } catch (Exception e) {
            Logging.error("getCoordinates", e);
            throw e;
        }
    }

    private void getPixelColor(int x, int y) {
        try {
            // TODO: Test marker precision on color_test.png
            Bitmap rootElementBitmap = ImageProcessing.loadBitmapFromView(imageView_markerImage);

            // Setting screen boundary programmatically
            x = x < 0 ? 0 : Math.min(x, imageWidth - 1);
            y = y < 0 ? 0 : Math.min(y, imageHeight - 1);

            int targetPixel = rootElementBitmap.getPixel(x, y);
            Log.e("Target pixel", "x: " + x + ", y: " + y);
            // TODO: Calculate new gradient based on adjusted value for eye/nose and save in gradientAndPositions object
            Log.e("Pixel color", Color.red(targetPixel) + "," + Color.green(targetPixel) + "," + Color.blue(targetPixel));
        } catch (Exception e) {
            Logging.error("getPixelColor", e);
            throw e;
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
        } catch (Exception e) {
            Logging.error("SetOnTouchListener", e);
            throw e;
        }
    }

    //region Navigation buttons
    public void backOnClick(View view) {
        try {
            Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Logging.error("backOnClick", e);
            throw e;
        }
    }

    public void submitOnClick(View view) {
        try {
            Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
            intent.putExtra("filename", filepath);
            intent.putExtra("imageHeight", imageHeight);
            intent.putExtra("imageWidth", imageWidth);
            Bundle bundle = new Bundle();
            bundle.putSerializable("gradientAndPositions", gradientAndPositions);
            intent.putExtras(bundle);
            startActivity(intent);
        } catch (Exception e) {
            Logging.error("submitOnClick", e);
            throw e;
        }
    }
    //endregion
}