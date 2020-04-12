package rubenkarim.com.masterthesisapp.Database.DataAccessObjects;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import rubenkarim.com.masterthesisapp.Database.Entities.Observation;

@Dao
public interface ObservationDao {
    @Query("SELECT * FROM Observation")
    List<Observation> getAllObservations();

    @Query("SELECT * FROM Observation WHERE patientOwnerId LIKE :cprNumber")
    List<Observation> findObservationsByCprNumber(String cprNumber);

    @Insert
    long insertObservation(Observation observation);

    @Query("DELETE FROM Observation")
    void deleteAllObservations();

    @Delete
    int deleteObservations(Observation... observations);
}
