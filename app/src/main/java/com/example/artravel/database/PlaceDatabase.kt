package com.example.artravel.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [(DBPlace::class)], version = 1
)
abstract class PlaceDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao

    companion object {
        @Volatile
        private var INSTANCE: PlaceDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): PlaceDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    PlaceDatabase::class.java, "places_database"
                ).build()
            }
            return INSTANCE!!
        }
    }
}