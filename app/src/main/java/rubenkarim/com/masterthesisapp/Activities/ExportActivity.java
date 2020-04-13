package rubenkarim.com.masterthesisapp.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Database.AppDatabase;
import rubenkarim.com.masterthesisapp.Database.Entities.Observation;
import rubenkarim.com.masterthesisapp.R;
import rubenkarim.com.masterthesisapp.Utilities.Animation;

public class ExportActivity extends AppCompatActivity {

    private LinearLayout linearLayout_exportActivity;
    private ProgressBar progressBar_exportProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        linearLayout_exportActivity = findViewById(R.id.linearLayout_ExportActivity);
        progressBar_exportProgressbar = findViewById(R.id.progressBar_exportLoadingAnimation);
    }

    public void createCsvOnClick(View view) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            runOnUiThread(() -> Animation.showLoadingAnimation(progressBar_exportProgressbar, null, null));

            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<Observation> observations = db.observationDao().getAllObservations();

            if (observations.isEmpty()) {
                runOnUiThread(() -> Animation.hideLoadingAnimation(progressBar_exportProgressbar, null, null));
                Snackbar.make(linearLayout_exportActivity, "Database is empty", Snackbar.LENGTH_SHORT).show();
                return;
            }

            File exportDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "/Masterthesisexports/");
            boolean isDirectoryCreated = exportDirectory.exists() || exportDirectory.mkdirs();

            if (isDirectoryCreated) {
                String dateTime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Timestamp(System.currentTimeMillis()));
                String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + "/Masterthesisexports/" + dateTime + ".csv";
                File file = new File(filepath);
                StringBuilder stringBuilder = new StringBuilder();

                addColumnNames(stringBuilder);

                for (Observation obs : observations) {
                    stringBuilder.append(obs.observationId)
                            .append(",")
                            .append(obs.cprnumber)
                            .append(",")
                            .append(obs.filepath)
                            .append(",")
                            .append(obs.filename)
                            .append(",")
                            .append(obs.gradient)
                            .append(",")
                            .append(obs.eyepositionx)
                            .append(",")
                            .append(obs.eyepositiony)
                            .append(",")
                            .append(obs.nosepositionx)
                            .append(",")
                            .append(obs.nosepositiony)
                            .append(",")
                            .append(obs.markersadjusted)
                            .append(System.lineSeparator());
                }

                try {
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.append(stringBuilder);
                    fileWriter.close();
                }
                catch (IOException e) {
                    runOnUiThread(() -> Animation.hideLoadingAnimation(progressBar_exportProgressbar, null, null));
                    Snackbar.make(linearLayout_exportActivity, "Couldn't write to csv file", Snackbar.LENGTH_SHORT).show();
                    return;
                }
            }
            else {
                runOnUiThread(() -> Animation.hideLoadingAnimation(progressBar_exportProgressbar, null, null));
                Snackbar.make(linearLayout_exportActivity, "Couldn't create export directory", Snackbar.LENGTH_SHORT).show();
                return;
            }

            runOnUiThread(() -> Animation.hideLoadingAnimation(progressBar_exportProgressbar, null, null));
            Snackbar.make(linearLayout_exportActivity, "CSV file created successfully", Snackbar.LENGTH_SHORT).show();
        });
    }

    private void addColumnNames(StringBuilder stringBuilder) {
        stringBuilder.append("observationId")
                .append(",")
                .append("cprNumber")
                .append(",")
                .append("filepath")
                .append(",")
                .append("filename")
                .append(",")
                .append("gradient")
                .append(",")
                .append("eyePositionX")
                .append(",")
                .append("eyePositionY")
                .append(",")
                .append("nosePositionX")
                .append(",")
                .append("nosePositionY")
                .append(",")
                .append("markersAdjusted")
                .append(System.lineSeparator());
    }

    public void clearDatabaseOnClick(View view) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Deleting database")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            runOnUiThread(() -> Animation.showLoadingAnimation(progressBar_exportProgressbar, null, null));

                            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                            int numberOfObservations = db.observationDao().getAllObservations().size();

                            if (numberOfObservations == 0) {
                                runOnUiThread(() -> Animation.hideLoadingAnimation(progressBar_exportProgressbar, null, null));
                                Snackbar.make(linearLayout_exportActivity, "Database already empty", Snackbar.LENGTH_SHORT).show();
                            }
                            else {
                                db.observationDao().deleteAllObservations();

                                if (db.observationDao().getAllObservations().isEmpty()) {
                                    runOnUiThread(() -> Animation.hideLoadingAnimation(progressBar_exportProgressbar, null, null));
                                    Snackbar.make(linearLayout_exportActivity, "Removed: " + numberOfObservations + " observation(s)", Snackbar.LENGTH_LONG).show();
                                }
                                else {
                                    runOnUiThread(() -> Animation.hideLoadingAnimation(progressBar_exportProgressbar, null, null));
                                    Snackbar.make(linearLayout_exportActivity, "Couldn't clear database", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void backOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
