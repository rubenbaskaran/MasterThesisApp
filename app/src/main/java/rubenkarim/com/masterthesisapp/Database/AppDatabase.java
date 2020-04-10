package rubenkarim.com.masterthesisapp.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

// TODO: Create db
// TODO: Create tables
// TODO: Create method for saving record
// TODO: Create method for loading record
// TODO: Create method for looking up CPR number
// TODO: Create method for encrypting CPR number

@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}
