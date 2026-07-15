package com.noamlewkowicz.carchecker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The local Room database that stores previously searched vehicles, used
 * to make the app offline-first: cached results show instantly, and the
 * network is only needed for vehicles that were never looked up before.
 */
@Database(
    entities = [CarDetailsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class CarDatabase : RoomDatabase() {

    abstract fun carDao(): CarDao

    companion object {
        @Volatile
        private var instance: CarDatabase? = null

        /**
         * Returns the single shared database instance, creating it on
         * first use.
         */
        fun getInstance(context: Context): CarDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CarDatabase::class.java,
                    "car_checker.db"
                ).build().also { instance = it }
            }
        }
    }
}
