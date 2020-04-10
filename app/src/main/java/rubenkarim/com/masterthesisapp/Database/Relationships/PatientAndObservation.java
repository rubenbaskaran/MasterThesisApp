package rubenkarim.com.masterthesisapp.Database.Relationships;

import androidx.room.Embedded;
import androidx.room.Relation;
import rubenkarim.com.masterthesisapp.Database.Entities.Observation;
import rubenkarim.com.masterthesisapp.Database.Entities.Patient;

public class PatientAndObservation {
    @Embedded
    public Patient patient;
    @Relation(
            parentColumn = "patientId",
            entityColumn = "patientOwnerId"
    )
    public Observation observation;
}
