package rubenkarim.com.masterthesisapp.Database;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import rubenkarim.com.masterthesisapp.Database.DataAccessObjects.ObservationDao;
import rubenkarim.com.masterthesisapp.Database.Entities.Observation;

@Database(entities = {Observation.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ObservationDao observationDao();

    // To run database operations asynchronously on a background thread.
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static volatile AppDatabase INSTANCE;
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "masterthesisapp_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
