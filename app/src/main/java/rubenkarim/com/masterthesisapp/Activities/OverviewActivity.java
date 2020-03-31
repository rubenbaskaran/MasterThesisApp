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
import rubenkarim.com.masterthesisapp.Utilities.GlobalVariables;
import rubenkarim.com.masterthesisapp.Utilities.MinMaxDataTransferContainer;

public class OverviewActivity extends AppCompatActivity {

    //region Properties
    private ImageView imageView_thermalImageContainer;
    private TextView textView_cprNumber;
    private int imageHeight;
    private int imageWidth;
    private String thermalImagePath;
    private GradientModel gradientAndPositions;
    private MinMaxDataTransferContainer minMaxData;
    private int imageViewVerticalOffset;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        imageView_thermalImageContainer = findViewById(R.id.imageView_patientImage);
        textView_cprNumber = findViewById(R.id.textView_cprNumber);

        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra("thermalImagePath")) {
            thermalImagePath = receivedIntent.getStringExtra("thermalImagePath");
        }
        if (receivedIntent.hasExtra("imageHeight")) {
            imageHeight = receivedIntent.getIntExtra("imageHeight", 0);
        }
        if (receivedIntent.hasExtra("imageWidth")) {
            imageWidth = receivedIntent.getIntExtra("imageWidth", 0);
        }
        if (receivedIntent.hasExtra("imageViewVerticalOffset")) {
            imageViewVerticalOffset = receivedIntent.getIntExtra("imageViewVerticalOffset", 0);
        }

        Bundle bundle = receivedIntent.getExtras();
        if (bundle != null) {
            gradientAndPositions = (GradientModel) bundle.getSerializable("gradientAndPositions");
            ((TextView) findViewById(R.id.textView_gradient)).setText(String.valueOf(gradientAndPositions.getGradient()));

            minMaxData = (MinMaxDataTransferContainer) bundle.getSerializable("minMaxData");
        }

        setPicture();
    }

    private void setPicture() {
        imageView_thermalImageContainer.setImageURI(Uri.parse(thermalImagePath));
    }

    public void backOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MarkerActivity.class);
        intent.putExtra("thermalImagePath", thermalImagePath);
        intent.putExtra("imageHeight", imageHeight);
        intent.putExtra("imageWidth", imageWidth);
        intent.putExtra("imageViewVerticalOffset", imageViewVerticalOffset);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gradientAndPositions", gradientAndPositions);
        addMinMaxDataIfChosen(bundle);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void addMinMaxDataIfChosen(Bundle bundle) {
        if (GlobalVariables.getCurrentAlgorithm() == GlobalVariables.Algorithms.MinMaxTemplate) {
            bundle.putSerializable("minMaxData", minMaxData);
        }
    }

    public void saveOnClick(View view) {
        String cpr = textView_cprNumber.getText().toString();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
