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
import rubenkarim.com.masterthesisapp.Utilities.Scaling;

// TODO: Refactor and optimize
public class MarkerActivity extends AppCompatActivity {
    ImageView imageView_markerImage;
    String filename = "default_picture";
    Boolean isThermalPicture = false;
    private static final String TAG = CameraActivity.class.getSimpleName();
    String eyeMarkerPath = "android.resource://rubenkarim.com.masterthesisapp/drawable/" + "eye_marker";
    String noseMarkerPath = "android.resource://rubenkarim.com.masterthesisapp/drawable/" + "nose_marker";
    int imageViewVerticalOffset;
    int imageHeight;
    int imageWidth;
    int markerWidthHeight = 200;
    View container;
    GradientModel gradientAndPositions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        imageView_markerImage = findViewById(R.id.imageView_markerImage);
        container = findViewById(R.id.linearLayout_MarkerActivity);

        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra("filename")) {
            filename = receivedIntent.getStringExtra("filename");
        }
        if (receivedIntent.hasExtra("isThermalImage")) {
            isThermalPicture = receivedIntent.getBooleanExtra("isThermalImage", true);
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

        setPicture();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void SetOnTouchListener(ImageView img) {
        img.setOnTouchListener(new View.OnTouchListener() {
            PointF DownPT = new PointF(); // Record Mouse Position When Pressed Down
            PointF StartPT = new PointF(); // Record Start Position of 'img'

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        img.setX((int) (StartPT.x + event.getX() - DownPT.x));
                        img.setY((int) (StartPT.y + event.getY() - DownPT.y));
                        StartPT.set(img.getX(), img.getY());
                        break;
                    case MotionEvent.ACTION_DOWN:
                        DownPT.set(event.getX(), event.getY());
                        StartPT.set(img.getX(), img.getY());
                        break;
                    case MotionEvent.ACTION_UP:
                        getCoordinates(img);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void setPicture() {
        ImageProcessing.FixImageOrientation(filename);
        int[] imageOriginalDimensions = null;

        if (isThermalPicture) {
            try {
                if (ThermalImageFile.isThermalImage(filename)) {
                    ThermalImageFile thermalImageFile = (ThermalImageFile) ImageFactory.createImage(filename);

                    JavaImageBuffer javaBuffer = thermalImageFile.getImage();
                    android.graphics.Bitmap originalThermalImageBitmap = BitmapAndroid.createBitmap(javaBuffer).getBitMap();
                    imageView_markerImage.setImageBitmap(originalThermalImageBitmap);
                    imageOriginalDimensions = new int[]{originalThermalImageBitmap.getWidth(), originalThermalImageBitmap.getHeight()};
                }
                else {
                    Log.e(TAG, "SetPicture: IS NOT A THERMAL PICTURE");
                }
            }
            catch (IOException e) {
                Log.e(TAG, "setPicture: IO ERROR: " + e.toString());
                //TODO: Handle IO exception
            }
        }
        else {
            imageView_markerImage.setImageURI(Uri.parse(filename));
            Bitmap originalRgbImageBitmap = ImageProcessing.convertToBitmap(filename);
            imageOriginalDimensions = new int[]{originalRgbImageBitmap.getWidth(), originalRgbImageBitmap.getHeight()};
        }

        addMarkers(imageOriginalDimensions, new int[]{imageWidth, imageHeight}, imageViewVerticalOffset);
    }

    private void addMarkers(int[] imageOriginalDimensions, int[] imageViewDimensions, int horizontalOffset) {

        RelativeLayout relativeLayout_markers = findViewById(R.id.relativeLayout_markers);
        ImageView imageView_eyeMarker = new ImageView(this);
        ImageView imageView_noseMarker = new ImageView(this);

        imageView_eyeMarker.setImageURI(Uri.parse(eyeMarkerPath));
        imageView_noseMarker.setImageURI(Uri.parse(noseMarkerPath));

        SetOnTouchListener(imageView_noseMarker);
        SetOnTouchListener(imageView_eyeMarker);

        RelativeLayout.LayoutParams eyeParams = new RelativeLayout.LayoutParams(markerWidthHeight, markerWidthHeight);
        int[] scaledEyeMarkerPosition = Scaling.getScaledMarkerPosition(gradientAndPositions.getEyePosition(), imageOriginalDimensions, imageViewDimensions, horizontalOffset);
        eyeParams.leftMargin = scaledEyeMarkerPosition[0] - markerWidthHeight/2;
        eyeParams.topMargin = scaledEyeMarkerPosition[1] - markerWidthHeight/2;
        relativeLayout_markers.addView(imageView_eyeMarker, eyeParams);

        RelativeLayout.LayoutParams noseParams = new RelativeLayout.LayoutParams(markerWidthHeight, markerWidthHeight);
        int[] scaledNoseMarkerPosition = Scaling.getScaledMarkerPosition(gradientAndPositions.getNosePosition(), imageOriginalDimensions, imageViewDimensions, horizontalOffset);
        noseParams.leftMargin = scaledNoseMarkerPosition[0] - markerWidthHeight/2;
        noseParams.topMargin = scaledNoseMarkerPosition[1] - markerWidthHeight/2;
        relativeLayout_markers.addView(imageView_noseMarker, noseParams);

        Log.e("TEST 2 (addMarkers)", "eye x: " + eyeParams.leftMargin + ", eye y: " + eyeParams.topMargin
                + ". nose x: " + noseParams.leftMargin + ", nose y: " + noseParams.topMargin
                + ". imageView x: " + imageWidth + ", imageView y: " + imageHeight
                + ". markerWidth: " + markerWidthHeight + ". horizontal offset: " + horizontalOffset);
    }

    private void getCoordinates(ImageView marker) {
        Bitmap markerBitmap = ((BitmapDrawable) marker.getDrawable()).getBitmap();
        int markerVerticalOffset = markerBitmap.getHeight();
        int markerHorizontalOffset = markerBitmap.getWidth();
        int[] coordinates = new int[2];
        marker.getLocationOnScreen(coordinates);

        int x = coordinates[0] + markerHorizontalOffset / 2;
        int y = coordinates[1] + markerVerticalOffset / 2 - imageViewVerticalOffset;
        Log.e(String.valueOf(marker.getTag()), "x: " + x + ", y: " + y);
        getPixelColor(x, y);
    }

    private void getPixelColor(int x, int y) {
        Bitmap rootElementBitmap = ImageProcessing.loadBitmapFromView(container);

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
        Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
        intent.putExtra("filename", filename);
        intent.putExtra("isThermalImage", isThermalPicture);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gradientAndPositions", gradientAndPositions);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    //endregion
}