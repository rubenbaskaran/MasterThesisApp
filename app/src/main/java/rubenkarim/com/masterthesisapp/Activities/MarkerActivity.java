package rubenkarim.com.masterthesisapp.Activities;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.flir.thermalsdk.androidsdk.image.BitmapAndroid;
import com.flir.thermalsdk.image.ImageFactory;
import com.flir.thermalsdk.image.JavaImageBuffer;
import com.flir.thermalsdk.image.ThermalImage;
import com.flir.thermalsdk.image.ThermalImageFile;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.R;

public class MarkerActivity extends AppCompatActivity
{
    ImageView imageView_markerImage;
    String filename = "default_picture";
    Boolean isThermalPicture;
    private static final String TAG = CameraActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        imageView_markerImage = findViewById(R.id.imageView_markerImage);

        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra("filename"))
        {
            filename = receivedIntent.getStringExtra("filename");
        }
        if (receivedIntent.hasExtra("isThermalImage")){
            isThermalPicture = receivedIntent.getBooleanExtra("isThermalImage", true);
        }

        SetPicture();
    }

    private void SetPicture()
    {

        if(isThermalPicture){
            try {
                if(ThermalImageFile.isThermalImage(filename)){
                    ThermalImageFile thermalImageFile = (ThermalImageFile) ImageFactory.createImage(filename);

                    JavaImageBuffer javaBuffer = thermalImageFile.getImage();
                    android.graphics.Bitmap bmp = BitmapAndroid.createBitmap(javaBuffer).getBitMap();
                    imageView_markerImage.setImageBitmap(bmp);

                } else {
                    Log.e(TAG, "SetPicture: IS NOT A THERMAL PICTURE");
                }
            } catch (IOException e) {
                //TODO: Handle IO exception
            }
        } else {
            imageView_markerImage.setImageURI(Uri.parse(filename));
        }




    }

    public void BackOnClick(View view)
    {
        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivity(intent);
    }

    public void SubmitOnClick(View view)
    {
        Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
        intent.putExtra("filename", filename);
        startActivity(intent);
    }
}