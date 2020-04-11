package rubenkarim.com.masterthesisapp.Database.DataAccessObjects;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Query;
import rubenkarim.com.masterthesisapp.Database.Entities.Observation;

@Dao
public interface ObservationDao {
    @Query("SELECT * FROM Observation")
    List<Observation> getAll();

    @Query("SELECT * FROM Observation WHERE patientOwnerId LIKE :cprNumber")
    List<Observation> findByCprNumber(String cprNumber);

    @Delete
    void delete(Observation observation);
}
