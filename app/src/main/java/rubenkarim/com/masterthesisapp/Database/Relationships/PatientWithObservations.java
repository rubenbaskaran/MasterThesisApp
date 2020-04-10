package rubenkarim.com.masterthesisapp.Database.Relationships;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;
import rubenkarim.com.masterthesisapp.Database.Entities.Observation;
import rubenkarim.com.masterthesisapp.Database.Entities.Patient;

public class PatientWithObservations {
    @Embedded
    public Patient patient;
    @Relation(
            parentColumn = "patientId",
            entityColumn = "patientOwnerId"
    )
    public List<Observation> observations;
}
