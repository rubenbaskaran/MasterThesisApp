package rubenkarim.com.masterthesisapp.Database.Entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"name"},
        unique = true)})
public class Algorithm {
    @PrimaryKey
    public long algorithmId;

    public String name;
}
