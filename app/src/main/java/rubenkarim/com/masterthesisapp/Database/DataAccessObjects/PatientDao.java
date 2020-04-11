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

    @Query("SELECT * FROM Patient WHERE cpr_number LIKE :cprNumber")
    Patient findByCprNumber(String cprNumber);

    @Insert
    void insertAll(Patient... patients);

    @Delete
    void delete(Patient patient);

    @Transaction
    @Query("SELECT * FROM Patient")
    public List<PatientWithObservations> getPatientsWithObservations();
}
