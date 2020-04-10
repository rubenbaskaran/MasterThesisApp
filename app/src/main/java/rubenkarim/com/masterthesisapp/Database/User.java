package rubenkarim.com.masterthesisapp.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"cpr_number"},
        unique = true)})
public class User {
    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "cpr_number")
    public String cprNumber;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;
}

