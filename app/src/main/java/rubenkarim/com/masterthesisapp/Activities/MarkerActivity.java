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
import android.widget.RelativeLayout;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.ImageFactory;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.ThermalImageFile;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;
import rubenkarim.com.masterthesisapp.Utilities.Logging;
import rubenkarim.com.masterthesisapp.Utilities.Scaling;

public class MarkerActivity extends AppCompatActivity {

    //region Properties
    private String filename;
    private Boolean isThermalCameraOn = false;
    private int imageViewVerticalOffset;
    private int imageHeight;
    private int imageWidth;
    private ImageView imageView_markerImage;
    private GradientModel gradientAndPositions;
    private boolean useDefaultPicture = false;
    private Uri defaultThermalPictureUri = null;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_marker);
            imageView_markerImage = findViewById(R.id.imageView_markerImage);

            Intent receivedIntent = getIntent();
            if (receivedIntent.hasExtra("filename")) {
                filename = receivedIntent.getStringExtra("filename");
            }
            if (receivedIntent.hasExtra("isThermalCameraOn")) {
                isThermalCameraOn = receivedIntent.getBooleanExtra("isThermalCameraOn", false);
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
            if (receivedIntent.hasExtra("useDefaultPicture")) {
                useDefaultPicture = receivedIntent.getBooleanExtra("useDefaultPicture", false);
                if (useDefaultPicture) {
                    defaultThermalPictureUri = Uri.parse("android.resource://" + this.getPackageName() + "/drawable/thermal_picture");
                }
            }

            Bundle bundle = receivedIntent.getExtras();
            if (bundle != null) {
                gradientAndPositions = (GradientModel) bundle.getSerializable("gradientAndPositions");
            }

            setPicture();
        }
        catch (Exception e) {
            Logging.error("onCreate", e);
            throw e;
        }
    }

    private void setPicture() {
        try {
            ImageProcessing.FixImageOrientation(filename);
            int[] imageOriginalDimensions = null;

            if (isThermalCameraOn) {
                try {
                    if (ThermalImageFile.isThermalImage(filename)) {
                        ThermalImageFile thermalImageFile = (ThermalImageFile) ImageFactory.createImage(filename);
                        JavaImageBuffer javaBuffer = thermalImageFile.getImage();
                        Bitmap originalThermalImageBitmap = BitmapAndroid.createBitmap(javaBuffer).getBitMap();
                        imageView_markerImage.setImageBitmap(originalThermalImageBitmap);
                        imageOriginalDimensions = new int[]{originalThermalImageBitmap.getWidth(), originalThermalImageBitmap.getHeight()};
                    }
                    else {
                        Log.e("SetPicture", "IS NOT A THERMAL PICTURE");
                    }
                }
                catch (IOException e) {
                    Log.e("setPicture", "IO ERROR: " + e.toString());
                }
            }
            else {
                imageView_markerImage.setImageURI(useDefaultPicture ? defaultThermalPictureUri : Uri.parse(filename));
                Bitmap originalRgbImageBitmap = useDefaultPicture ?
                        ImageProcessing.convertDrawableToBitmap(this, R.drawable.rgb_picture)
                        : ImageProcessing.convertToBitmap(filename);
                imageOriginalDimensions = new int[]{originalRgbImageBitmap.getWidth(), originalRgbImageBitmap.getHeight()};
            }

            addMarkers(imageOriginalDimensions, new int[]{imageWidth, imageHeight}, imageViewVerticalOffset);
        }
        catch (Exception e) {
            Logging.error("setPicture", e);
            throw e;
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
        }
        catch (Exception e) {
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
        }
        catch (Exception e) {
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
        }
        catch (Exception e) {
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
        }
        catch (Exception e) {
            Logging.error("SetOnTouchListener", e);
            throw e;
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
            throw e;
        }
    }

    public void submitOnClick(View view) {
        try {
            Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
            intent.putExtra("filename", filename);
            intent.putExtra("isThermalCameraOn", isThermalCameraOn);
            intent.putExtra("useDefaultPicture", useDefaultPicture);
            intent.putExtra("imageHeight", imageHeight);
            intent.putExtra("imageWidth", imageWidth);
            Bundle bundle = new Bundle();
            bundle.putSerializable("gradientAndPositions", gradientAndPositions);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        catch (Exception e) {
            Logging.error("submitOnClick", e);
            throw e;
        }
    }
    //endregion
}