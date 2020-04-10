package rubenkarim.com.masterthesisapp.Database.Entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"cpr_number"},
        unique = true)})
public class Patient {
    @PrimaryKey
    public int patientId;

    @ColumnInfo(name = "cpr_number")
    public String cprNumber;
}

