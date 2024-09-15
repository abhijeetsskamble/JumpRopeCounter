import androidx.room.*

@Dao
interface JumpDataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertJumpData(jumpData: JumpData)

    @Query("SELECT * FROM jump_data ORDER BY 1 DESC LIMIT 7")
    fun getJumpData(): List<JumpData>
}

