package dev.gaborbiro.investments.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.ZoneId

@Entity(tableName = "records", indices = [Index(value = ["day", "zoneId"], unique = true)])
class RecordDBModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    /**
     * Day at which the record was generated. Conflicting writes will override the record.
     * The idea is to keep the last seen record of the day
     */
    val day: LocalDate,

    /**
     * Timezone of [day]
     */
    val zoneId: ZoneId,

    /**
     * JSON of key value pairs
     */
    val record: String,
)