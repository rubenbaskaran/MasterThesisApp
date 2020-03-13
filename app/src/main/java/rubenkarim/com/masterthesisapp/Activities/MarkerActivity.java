package rubenkarim.com.masterthesisapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.R;

public class MarkerActivity extends AppCompatActivity
{
    ImageView imageView_markerImage;
    String filename = "android.resource://rubenkarim.com.masterthesisapp/drawable/" + "default_picture";
    String marker = "android.resource://rubenkarim.com.masterthesisapp/drawable/" + "marker";
    ImageView imageView_markerOne;
    ImageView imageView_markerTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker);
        imageView_markerImage = findViewById(R.id.imageView_markerImage);
        imageView_markerOne = findViewById(R.id.imageView_markerOne);
        imageView_markerTwo = findViewById(R.id.imageView_markerTwo);

        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra("filename"))
        {
            filename = receivedIntent.getStringExtra("filename");
        }

        SetPicture();
    }

    private void SetPicture()
    {
        imageView_markerImage.setImageURI(Uri.parse(filename));
        imageView_markerOne.setImageURI(Uri.parse(marker));
        imageView_markerTwo.setImageURI(Uri.parse(marker));
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

    private void GetCoordinates()
    {
        int[] coordinatesOne = new int[2];
        imageView_markerOne.getLocationOnScreen(coordinatesOne);
        Log.e("MarkerOne location", "x: " + String.valueOf(coordinatesOne[0]) + ", y: " + String.valueOf(coordinatesOne[1]));

        int[] coordinatesTwo = new int[2];
        imageView_markerTwo.getLocationOnScreen(coordinatesTwo);
        Log.e("MarkerTwo location", "x: " + String.valueOf(coordinatesTwo[0]) + ", y: " + String.valueOf(coordinatesTwo[1]));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        GetCoordinates();
    }
}