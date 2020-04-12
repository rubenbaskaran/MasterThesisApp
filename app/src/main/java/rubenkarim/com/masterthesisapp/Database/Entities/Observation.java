package rubenkarim.com.masterthesisapp.Database.Entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Observation {
    @PrimaryKey(autoGenerate = true)
    public long observationId;

    @NonNull
    public long patientOwnerId;

    @NonNull
    public String filepath;

    @NonNull
    public String filename;

    @NonNull
    public int eyepositionx;

    @NonNull
    public int eyepositiony;

    @NonNull
    public int nosepositionx;

    @NonNull
    public int nosepositiony;

    @NonNull
    public double gradient;
}
