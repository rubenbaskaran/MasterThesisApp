package rubenkarim.com.masterthesisapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Algorithms.MinMaxAlgorithm;
import rubenkarim.com.masterthesisapp.Models.RoiModel;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.GlobalVariables;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MinMaxAlgorithm minMaxAlgorithm = new MinMaxAlgorithm(
                "/storage/emulated/0/Pictures/Masterthesisimages/14:14:25.jpg",
                new RoiModel(new int[]{0, 280}, 210, 210),
                new RoiModel(new int[]{0, 280}, 210, 210),
                new RoiModel(new int[]{0, 280}, 210, 210)
        );

        minMaxAlgorithm.calculateGradient();
    }

    public void ChooseAlgorithmOnClick(View view) {
        int idOfChosenAlgorithm = Integer.parseInt(String.valueOf(view.getTag()));
        GlobalVariables.Algorithms chosenAlgorithm = GlobalVariables.Algorithms.values()[idOfChosenAlgorithm];
        GlobalVariables.setCurrentAlgorithm(chosenAlgorithm);
        Log.e("Chosen algorithm", GlobalVariables.getCurrentAlgorithm().toString());

        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivity(intent);
    }
}
