package dev.gaborbiro.investments.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [RecordDBModel::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DB : RoomDatabase() {

    abstract fun recordsDAO(): RecordsDAO

    companion object {

        @Volatile
        private var INSTANCE: DB? = null

        fun getInstance(context: Context): DB =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): DB {
            return Room.databaseBuilder(
                context.applicationContext,
                DB::class.java,
                "investments_db"
            ).fallbackToDestructiveMigration() // TODO: Remove before shipping release
                .build()
        }
    }
}