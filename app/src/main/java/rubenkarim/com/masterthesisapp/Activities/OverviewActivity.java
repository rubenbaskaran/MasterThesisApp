package rubenkarim.com.masterthesisapp.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.R;

public class OverviewActivity extends AppCompatActivity {
    private static final String TAG = OverviewActivity.class.getSimpleName();
    private ImageView imageView_patientImage;
    private String filename = "android.resource://rubenkarim.com.masterthesisapp/drawable/" + "default_picture";
    private Boolean isThermalCameraOn = false;
    private GradientModel gradientAndPositions;
    private int imageHeight;
    private int imageWidth;
    private TextView cprTextField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        cprTextField = findViewById(R.id.cprTextField);
        imageView_patientImage = findViewById(R.id.imageView_patientImage);
        TextView textView_gradient = findViewById(R.id.textView_gradient);

        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra("filename")) {
            filename = receivedIntent.getStringExtra("filename");
        }
        if (receivedIntent.hasExtra("isThermalCameraOn")) {
            isThermalCameraOn = receivedIntent.getBooleanExtra("isThermalCameraOn", false);
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
            textView_gradient.setText(String.valueOf(gradientAndPositions.getGradient()));
        }

        setPicture();
    }

    private void setPicture() {
        imageView_patientImage.setImageURI(Uri.parse(filename));
    }

    public void backOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MarkerActivity.class);
        intent.putExtra("filename", filename);
        intent.putExtra("isThermalCameraOn", isThermalCameraOn);
        intent.putExtra("imageHeight", imageHeight);
        intent.putExtra("imageWidth", imageWidth);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gradientAndPositions", gradientAndPositions);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void saveOnClick(View view) {

        String cpr = cprTextField.getText().toString();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
