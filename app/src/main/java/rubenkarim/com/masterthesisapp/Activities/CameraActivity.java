package rubenkarim.com.masterthesisapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class CameraActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
    }

    public void BackOnClick(View view)
    {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void TakePictureOnClick(View view)
    {
        Intent intent = new Intent(getApplicationContext(), MarkerActivity.class);
        startActivity(intent);
    }
}
