package dev.gaborbiro.investments.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecordsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(records: Iterable<RecordDBModel>)

    @Query("SELECT * FROM records")
    suspend fun get(): List<RecordDBModel>
}