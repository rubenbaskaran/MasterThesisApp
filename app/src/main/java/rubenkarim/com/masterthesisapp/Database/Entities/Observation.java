package rubenkarim.com.masterthesisapp.Database.Entities;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Observation {
    @PrimaryKey
    public long observationId;

    public long patientOwnerId;

    @Embedded
    public Patient patient;
}
