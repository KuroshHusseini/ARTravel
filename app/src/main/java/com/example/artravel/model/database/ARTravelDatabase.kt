package com.example.artravel.model.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.artravel.model.converter.Converters
import com.example.artravel.model.dao.AttractionDao
import com.example.artravel.model.entity.DBPlace
import com.example.artravel.model.dao.FavouritesDao
import com.example.artravel.model.entity.DBAttraction

@Database(entities = [DBPlace::class, DBAttraction::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class ARTravelDatabase : RoomDatabase() {
    abstract fun favouriteDao(): FavouritesDao
    abstract fun attractionDao(): AttractionDao

    companion object {
        @Volatile
        private var INSTANCE: ARTravelDatabase? = null

        fun getDatabase(context: Context): ARTravelDatabase {
            val tempInstance = INSTANCE
            Log.d("DBG", "tempInstance = INSTANCE")

            if (tempInstance != null) {
                return tempInstance
            }
            Log.d("DBG", "tempInstance is not NULL")
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ARTravelDatabase::class.java,
                    "database.db"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}