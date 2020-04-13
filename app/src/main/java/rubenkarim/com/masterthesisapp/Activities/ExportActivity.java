package rubenkarim.com.masterthesisapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Database.AppDatabase;
import rubenkarim.com.masterthesisapp.Database.Entities.Observation;
import rubenkarim.com.masterthesisapp.R;

public class ExportActivity extends AppCompatActivity {

    LinearLayout linearLayout_exportActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        linearLayout_exportActivity = findViewById(R.id.linearLayout_ExportActivity);
    }

    public void createCsvOnClick(View view) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<Observation> observations = db.observationDao().getAllObservations();

            if (observations.isEmpty()) {
                Snackbar.make(linearLayout_exportActivity, "Database is empty", Snackbar.LENGTH_SHORT).show();
                return;
            }

            File exportDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "/Masterthesisexports/");
            boolean isDirectoryCreated = exportDirectory.exists() || exportDirectory.mkdirs();

            if (isDirectoryCreated) {
                String dateTime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Timestamp(System.currentTimeMillis()));
                String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + "/Masterthesisexports/output_" + dateTime + ".csv";
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
                            .append(System.lineSeparator());
                }

                try {
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.append(stringBuilder);
                    fileWriter.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                Snackbar.make(linearLayout_exportActivity, "Couldn't create export directory", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void addColumnNames(StringBuilder stringBuilder) {
        stringBuilder.append("observationId")
                .append(",")
                .append("cprnumber")
                .append(",")
                .append("filepath")
                .append(",")
                .append("filename")
                .append(",")
                .append("gradient")
                .append(",")
                .append("eyepositionx")
                .append(",")
                .append("eyepositiony")
                .append(",")
                .append("nosepositionx")
                .append(",")
                .append("nosepositiony")
                .append(System.lineSeparator());
    }

    public void clearDatabaseOnClick(View view) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            int numberOfObservations = db.observationDao().getAllObservations().size();
            String outputMessageOnSuccess = "Removed: " + numberOfObservations + " observation(s)";

            if (numberOfObservations == 0) {
                Snackbar.make(linearLayout_exportActivity, "Database already empty", Snackbar.LENGTH_SHORT).show();
            }
            else {
                db.observationDao().deleteAllObservations();

                if (db.observationDao().getAllObservations().isEmpty()) {
                    Snackbar.make(linearLayout_exportActivity, outputMessageOnSuccess, Snackbar.LENGTH_LONG).show();
                }
                else {
                    Snackbar.make(linearLayout_exportActivity, "Couldn't clear database", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    public void backOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
