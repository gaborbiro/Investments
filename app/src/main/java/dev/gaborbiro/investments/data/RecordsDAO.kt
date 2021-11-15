package dev.gaborbiro.investments.data

import androidx.room.*
import dev.gaborbiro.investments.data.model.RecordDBModel
import java.time.LocalDate

@Dao
interface RecordsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(records: Iterable<RecordDBModel>)

    @Query("SELECT * FROM records WHERE day > :since")
    suspend fun get(since: LocalDate): List<RecordDBModel>

    @Query("SELECT * FROM records WHERE id=:id")
    suspend fun get(id: String): RecordDBModel?

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(record: RecordDBModel)

}