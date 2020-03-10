package rubenkarim.com.masterthesisapp.Activities;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.R;

public class MarkerActivity extends AppCompatActivity
{
    ImageView imageView_markerImage;
    String filename = "default_picture";

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

        SetPicture();
    }

    private void SetPicture()
    {
        imageView_markerImage.setImageURI(Uri.parse(filename));
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