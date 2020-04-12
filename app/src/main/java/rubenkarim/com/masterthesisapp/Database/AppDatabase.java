package rubenkarim.com.masterthesisapp.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import rubenkarim.com.masterthesisapp.Database.DataAccessObjects.ObservationDao;
import rubenkarim.com.masterthesisapp.Database.DataAccessObjects.PatientDao;
import rubenkarim.com.masterthesisapp.Database.Entities.Observation;
import rubenkarim.com.masterthesisapp.Database.Entities.Patient;

// TODO: Create db
// TODO: Create tables
// TODO: Create method for saving record
// TODO: Create method for loading record
// TODO: Create method for looking up CPR number
// TODO: Create method for encrypting CPR number

@Database(entities = {Patient.class, Observation.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PatientDao patientDao();

    public abstract ObservationDao observationDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "database-name")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
