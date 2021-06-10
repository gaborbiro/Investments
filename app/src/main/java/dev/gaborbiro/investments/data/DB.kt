package dev.gaborbiro.investments.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.gaborbiro.investments.data.model.RecordDBModel

@Database(entities = [RecordDBModel::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DB : RoomDatabase() {

    abstract fun recordsDAO(): RecordsDAO

    companion object {

        @Volatile
        private lateinit var INSTANCE: DB

        fun init(appContext: Context) {
            INSTANCE = buildDatabase(appContext)
        }

        fun getInstance(): DB = INSTANCE

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