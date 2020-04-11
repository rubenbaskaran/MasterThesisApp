package rubenkarim.com.masterthesisapp.Database;

import androidx.room.Database;
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
}
