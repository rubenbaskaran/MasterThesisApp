package rubenkarim.com.masterthesisapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.R;

public class OverviewActivity extends AppCompatActivity
{
    ImageView imageView_patientImage;
    String filename = "android.resource://rubenkarim.com.masterthesisapp/drawable/" + "default_picture";
    Boolean isThermalPicture = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        imageView_patientImage = findViewById(R.id.imageView_patientImage);

        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra("filename"))
        {
            filename = receivedIntent.getStringExtra("filename");
        }
        if (receivedIntent.hasExtra("isThermalImage")){
            isThermalPicture = receivedIntent.getBooleanExtra("isThermalImage", true);
        }

        setPicture();
    }

    private void setPicture()
    {
        imageView_patientImage.setImageURI(Uri.parse(filename));
    }

    public void backOnClick(View view)
    {
        Intent intent = new Intent(getApplicationContext(), MarkerActivity.class);
        intent.putExtra("filename", filename);
        intent.putExtra("isThermalImage", isThermalPicture);
        startActivity(intent);
    }

    public void saveOnClick(View view)
    {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
