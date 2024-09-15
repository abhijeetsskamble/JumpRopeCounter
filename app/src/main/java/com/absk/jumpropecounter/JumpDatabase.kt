import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [JumpData::class], version = 1, exportSchema = false)
abstract class JumpDatabase : RoomDatabase() {

    abstract fun jumpDataDao(): JumpDataDao

    companion object {
        @Volatile
        private var INSTANCE: JumpDatabase? = null

        fun getDatabase(context: Context): JumpDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JumpDatabase::class.java,
                    "jump_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
