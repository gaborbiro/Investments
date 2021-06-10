package dev.gaborbiro.investments.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    @JvmStatic
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let {
            return formatter.parse(value, LocalDate::from)
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    @JvmStatic
    fun toZoneId(value: String?): ZoneId? {
        return value?.let {
            return ZoneId.of(it)
        }
    }

    @TypeConverter
    @JvmStatic
    fun fromZoneId(zoneId: ZoneId?): String? {
        return zoneId?.id
    }
}