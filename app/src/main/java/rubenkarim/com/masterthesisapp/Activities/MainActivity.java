package rubenkarim.com.masterthesisapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.GlobalVariables;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout linearLayout = findViewById(R.id.linearLayout_MainActivity);
        Intent receivedIntent = getIntent();
        if (receivedIntent.getBooleanExtra("CameraNotFound", false)){
            Snackbar.make(linearLayout, "Connect FLIR camera and try again", Snackbar.LENGTH_SHORT).show();
        }
    }

    public void chooseAlgorithmOnClick(View view) {
        int idOfChosenAlgorithm = Integer.parseInt(String.valueOf(view.getTag()));
        GlobalVariables.Algorithms chosenAlgorithm = GlobalVariables.Algorithms.values()[idOfChosenAlgorithm];
        GlobalVariables.setCurrentAlgorithm(chosenAlgorithm);
        Log.e("Chosen algorithm", GlobalVariables.getCurrentAlgorithm().toString());

        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivity(intent);
    }

    public void exportDataOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), ExportActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        System.exit(0);
    }
}
