package rubenkarim.com.masterthesisapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import androidx.appcompat.app.AppCompatActivity;
import rubenkarim.com.masterthesisapp.Database.AppDatabase;
import rubenkarim.com.masterthesisapp.R;

public class ExportActivity extends AppCompatActivity {

    LinearLayout linearLayout_exportActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        linearLayout_exportActivity = findViewById(R.id.linearLayout_ExportActivity);
    }

    // TODO: Get data from db and save to csv
    public void createCsvOnClick(View view) {
//        AppDatabase.databaseWriteExecutor.execute(() -> {
//            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
//            List<Patient> patients = db.patientDao().getAllPatients();
//            List<Observation> observations = db.observationDao().getAllObservations();
//
//            Patient patient = new Patient();
//            patient.cprNumber = cpr.isEmpty() ? "0123456789" : cpr;
//            db.patientDao().insertPatient(patient);
//            Patient person = db.patientDao().findPatientByCprNumber("0123456789");
//        });

//        String filepath = "masterthesisapp" + File.separator + "DateTimeNow" + ".csv";
//        File file = new File(filepath);
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append("test");
//
//        try {
//            FileWriter fileWriter = new FileWriter(file);
//            fileWriter.append(stringBuilder);
//            fileWriter.close();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
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
