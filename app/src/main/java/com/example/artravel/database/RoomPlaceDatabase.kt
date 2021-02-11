package com.example.artravel.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [(RoomPlace::class)], version = 1
)
abstract class RoomPlaceDatabase : RoomDatabase() {
    abstract fun placeDao(): DBDao

    companion object {
        private var sInstance: RoomPlaceDatabase? = null

        @Synchronized
        fun get(context: Context): RoomPlaceDatabase {
            if (sInstance == null) {
                sInstance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        RoomPlaceDatabase::class.java, "place.db"
                    ).build()
            }
            return sInstance!!
        }
    }
}