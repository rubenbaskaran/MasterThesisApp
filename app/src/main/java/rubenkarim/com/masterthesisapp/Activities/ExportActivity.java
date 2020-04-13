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
import java.util.concurrent.atomic.AtomicReference;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Database.AppDatabase;
import rubenkarim.com.masterthesisapp.Database.Entities.Observation;
import rubenkarim.com.masterthesisapp.Database.Entities.Patient;
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
            List<Patient> patients = db.patientDao().getAllPatients();
            List<Observation> observations = db.observationDao().getAllObservations();

            File logDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "/Masterthesisexports/");
            boolean isDirectoryCreated = logDirectory.exists() || logDirectory.mkdirs();

            if (isDirectoryCreated) {
                String dateTime = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Timestamp(System.currentTimeMillis()));
                String filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + "/Masterthesisexports/output_" + dateTime + ".csv";
                File file = new File(filepath);
                StringBuilder stringBuilder = new StringBuilder();

                // TODO: Iterate through all observations
                stringBuilder.append(dateTime).append(",").append(patients.get(0).patientId).append(",").append(patients.get(0).cprNumber);

                try {
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.append(stringBuilder);
                    fileWriter.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void clearDatabaseOnClick(View view) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            int numberOfPatients = db.patientDao().getAllPatients().size();
            int numberOfObservations = db.observationDao().getAllObservations().size();
            String outputMessageOnSuccess = "Removed: " + numberOfPatients + " patient(s) and " + numberOfObservations + " observation(s)";

            if (numberOfPatients == 0 && numberOfObservations == 0) {
                Snackbar.make(linearLayout_exportActivity, "Database already empty", Snackbar.LENGTH_SHORT).show();
            }
            else {
                db.patientDao().deleteAllPatients();
                db.observationDao().deleteAllObservations();

                if (db.patientDao().getAllPatients().isEmpty() && db.observationDao().getAllObservations().isEmpty()) {
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
