package rubenkarim.com.masterthesisapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.R;

public class OverviewActivity extends AppCompatActivity {
    ImageView imageView_patientImage;
    String filename = "android.resource://rubenkarim.com.masterthesisapp/drawable/" + "default_picture";
    Boolean isThermalPicture = false;
    GradientModel gradientAndPositions;
    private boolean useDefaultPicture;
    private Uri defaultThermalPictureUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        imageView_patientImage = findViewById(R.id.imageView_patientImage);
        TextView textView_gradient = findViewById(R.id.textView_gradient);

        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra("filename")) {
            filename = receivedIntent.getStringExtra("filename");
        }
        if (receivedIntent.hasExtra("isThermalImage")) {
            isThermalPicture = receivedIntent.getBooleanExtra("isThermalImage", true);
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
            textView_gradient.setText(String.valueOf(gradientAndPositions.getGradient()));
        }

        setPicture();
    }

    private void setPicture() {
        imageView_patientImage.setImageURI(useDefaultPicture ? defaultThermalPictureUri : Uri.parse(filename));
    }

    public void backOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MarkerActivity.class);
        intent.putExtra("filename", filename);
        intent.putExtra("isThermalImage", isThermalPicture);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gradientAndPositions", gradientAndPositions);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void saveOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
