package rubenkarim.com.masterthesisapp.Database.Entities;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Observation {
    @PrimaryKey(autoGenerate = true)
    public long observationId;

    public long patientOwnerId;
}
