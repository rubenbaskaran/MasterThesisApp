package rubenkarim.com.masterthesisapp.Activities;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.R;

public class OverviewActivity extends AppCompatActivity
{
    ImageView imageView_patientImage;

    @Override

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        imageView_patientImage = findViewById(R.id.imageView_patientImage);
        SetPicture("test_picture");
    }

    private void SetPicture(String filename)
    {
        Resources resources = getApplicationContext().getResources();
        int resourceId = resources.getIdentifier(filename, "drawable", getApplicationContext().getPackageName());
        imageView_patientImage.setImageResource(resourceId);
    }
}
