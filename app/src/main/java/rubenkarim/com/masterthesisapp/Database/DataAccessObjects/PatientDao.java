package rubenkarim.com.masterthesisapp.Database.DataAccessObjects;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import rubenkarim.com.masterthesisapp.Database.Entities.Patient;
import rubenkarim.com.masterthesisapp.Database.Relationships.PatientWithObservations;

@Dao
public interface PatientDao {
    @Query("SELECT * FROM Patient")
    List<Patient> getAll();

    @Query("SELECT * FROM Patient WHERE patientId IN (:patientIds)")
    List<Patient> loadAllByIds(int[] patientIds);

    @Query("SELECT * FROM Patient WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    Patient findByName(String first, String last);

    @Insert
    void insertAll(Patient... patients);

    @Delete
    void delete(Patient patient);

    @Transaction
    @Query("SELECT * FROM Patient")
    public List<PatientWithObservations> getPatientsWithObservations();
}
