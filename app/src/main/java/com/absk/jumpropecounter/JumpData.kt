import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jump_data")
data class JumpData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val jumpCount: Int,
    val timeTaken: Long,
    val date: String // Date stored as timestamp (milliseconds since epoch)
)
