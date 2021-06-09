package dev.gaborbiro.investments.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(tableName = "records", indices = [Index(value = ["timestamp"], unique = true)])
class RecordDBModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    /**
     * Moment at which the record was generated
     */
    val timestamp: OffsetDateTime,

    /**
     * JSON of key value pairs
     */
    val record: String,
)