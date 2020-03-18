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

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.ImageFactory;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.ThermalImageFile;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;

public class MarkerActivity extends AppCompatActivity {
    ImageView imageView_markerImage;
    String filename = "default_picture";
    Boolean isThermalPicture = false;
    private static final String TAG = CameraActivity.class.getSimpleName();
    String marker = "android.resource://rubenkarim.com.masterthesisapp/drawable/" + "marker";
    ImageView imageView_markerOne;
    ImageView imageView_markerTwo;
    int imageViewVerticalOffset;
    int imageHeight;
    int imageWidth;
    ImageView imageView_leftEye;
    View container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        imageView_markerImage = findViewById(R.id.imageView_markerImage);
        imageView_markerOne = findViewById(R.id.imageView_markerOne);
        imageView_markerTwo = findViewById(R.id.imageView_markerTwo);
        SetOnTouchListener(imageView_markerOne);
        SetOnTouchListener(imageView_markerTwo);
        container = findViewById(R.id.linearLayout_MarkerActivity);
        imageView_leftEye = findViewById(R.id.imageView_leftEye);

        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra("filename")) {
            filename = receivedIntent.getStringExtra("filename");
        }
        if (receivedIntent.hasExtra("isThermalImage")) {
            isThermalPicture = receivedIntent.getBooleanExtra("isThermalImage", true);
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
        imageView_markerImage.setImageURI(Uri.parse(filename));
        imageView_markerOne.setImageURI(Uri.parse(marker));
        imageView_markerTwo.setImageURI(Uri.parse(marker));

        if (isThermalPicture) {
            try {
                if (ThermalImageFile.isThermalImage(filename)) {
                    ThermalImageFile thermalImageFile = (ThermalImageFile) ImageFactory.createImage(filename);

                    JavaImageBuffer javaBuffer = thermalImageFile.getImage();
                    android.graphics.Bitmap bmp = BitmapAndroid.createBitmap(javaBuffer).getBitMap();
                    imageView_markerImage.setImageBitmap(bmp);

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
        }
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        int[] coordinates = new int[2];
        imageView_markerImage.getLocationOnScreen(coordinates);
        imageViewVerticalOffset = coordinates[1];
        imageHeight = imageView_markerImage.getHeight();
        imageWidth = imageView_markerImage.getWidth();
        Log.e("Image dimensions", "x: " + imageWidth + ", y: " + imageHeight);
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
        startActivity(intent);
    }
    //endregion
}