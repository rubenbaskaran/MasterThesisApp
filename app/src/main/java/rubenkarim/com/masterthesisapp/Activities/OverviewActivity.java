package rubenkarim.com.masterthesisapp.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flir.thermalsdk.image.ImageFactory;
import com.flir.thermalsdk.image.ThermalImageFile;
import com.flir.thermalsdk.image.fusion.FusionMode;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Database.AppDatabase;
import rubenkarim.com.masterthesisapp.Database.Entities.Observation;
import rubenkarim.com.masterthesisapp.Models.GradientModel;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.Animation;
import rubenkarim.com.masterthesisapp.Utilities.GlobalVariables;
import rubenkarim.com.masterthesisapp.Utilities.ImageProcessing;
import rubenkarim.com.masterthesisapp.Utilities.Logging;

public class OverviewActivity extends AppCompatActivity {

    //region Properties
    private static final String TAG = OverviewActivity.class.getSimpleName();
    private ImageView imageView_thermalImageContainer;
    private TextView textView_cprNumber;
    private int imageHeight;
    private int imageWidth;
    private String mThermalImagePath;
    private GradientModel mGradientModel;
    private int imageViewVerticalOffset;
    private View mRootView;
    private ProgressBar progressBar_overviewProgressbar;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        imageView_thermalImageContainer = findViewById(R.id.imageView_patientImage);
        textView_cprNumber = findViewById(R.id.textView_cprNumber);
        mRootView = findViewById(R.id.LinearLayout_rootView);
        progressBar_overviewProgressbar = findViewById(R.id.progressBar_overviewLoadingAnimation);

        Intent receivedIntent = getIntent();
        if (receivedIntent.hasExtra("thermalImagePath")) {
            mThermalImagePath = receivedIntent.getStringExtra("thermalImagePath");
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
            mGradientModel = (GradientModel) bundle.getSerializable("gradientAndPositions");
            ((TextView) findViewById(R.id.textView_gradient)).setText(String.valueOf(mGradientModel.getGradient()));
        }

        try {
            ThermalImageFile thermalImage = (ThermalImageFile) ImageFactory.createImage(mThermalImagePath);
            setPicture(thermalImage, mGradientModel);
        }
        catch (IOException e) {
            Logging.error(this,TAG + " onCreate: ", e);
            Snackbar.make(mRootView, "There was an error with the thermal image file", Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    private void drawCirclesOnBitmap(Bitmap bitmap, int[] eye, int[] nose) {
        for (int i = 0; i < 10; i++) {
            bitmap.setPixel(eye[0] + i, eye[1], Color.RED);
            bitmap.setPixel(eye[0] - i, eye[1], Color.RED);
            bitmap.setPixel(eye[0], eye[1] + i, Color.RED);
            bitmap.setPixel(eye[0], eye[1] - i, Color.RED);
            bitmap.setPixel(nose[0] + i, nose[1], Color.RED);
            bitmap.setPixel(nose[0] - i, nose[1], Color.RED);
            bitmap.setPixel(nose[0], nose[1] + i, Color.RED);
            bitmap.setPixel(nose[0], nose[1] - i, Color.RED);
        }
    }

    private void setPicture(ThermalImageFile thermalImageFile, GradientModel gradientModel) {
        thermalImageFile.getFusion().setFusionMode(FusionMode.THERMAL_ONLY);
        Bitmap bmp = ImageProcessing.getBitmap(thermalImageFile);
        drawCirclesOnBitmap(bmp, gradientModel.getEyePosition(), gradientModel.getNosePosition());
        imageView_thermalImageContainer.setImageBitmap(bmp);
    }

    public void backOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MarkerActivity.class);
        intent.putExtra("thermalImagePath", mThermalImagePath);
        intent.putExtra("imageHeight", imageHeight);
        intent.putExtra("imageWidth", imageWidth);
        intent.putExtra("imageViewVerticalOffset", imageViewVerticalOffset);
        Bundle bundle = new Bundle();
        bundle.putSerializable("gradientAndPositions", mGradientModel);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void saveOnClick(View view) {
        String cprNumber = textView_cprNumber.getText().toString();

        if(cprNumber.isEmpty()){
            Snackbar.make(mRootView, "Please enter CPR number", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String[] filepathSplitted = mThermalImagePath.split("/");
        String filename = filepathSplitted[filepathSplitted.length-1];

        AppDatabase.databaseWriteExecutor.execute(() -> {
            runOnUiThread(() -> Animation.showLoadingAnimation(progressBar_overviewProgressbar, null));

            Observation observation = new Observation();
            observation.cprnumber = cprNumber;
            observation.filepath = mThermalImagePath;
            observation.filename = filename;
            observation.gradient = mGradientModel.getGradient();
            observation.eyepositionx = mGradientModel.getEyePosition()[0];
            observation.eyepositiony = mGradientModel.getEyePosition()[1];
            observation.nosepositionx = mGradientModel.getNosePosition()[0];
            observation.nosepositiony = mGradientModel.getNosePosition()[1];
            observation.eyeTemperature = mGradientModel.getEyeTemperature();
            observation.noseTemperature = mGradientModel.getNoseTemperature();
            observation.eyemarkeradjusted = mGradientModel.isEyeMarkerAdjusted();
            observation.nosemarkeradjusted = mGradientModel.isNoseMarkerAdjusted();
            observation.chosenAlgorithm = String.valueOf(GlobalVariables.getCurrentAlgorithm());

            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            db.observationDao().insertObservation(observation);

            runOnUiThread(() -> Animation.hideLoadingAnimation(progressBar_overviewProgressbar, null));

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });
    }
}
