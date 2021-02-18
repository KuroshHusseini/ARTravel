package com.example.artravel.favourites

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.artravel.Converters
import com.example.artravel.database.DBPlace

@Database(entities = [DBPlace::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FavouritesDatabase : RoomDatabase() {
    
    abstract fun favouriteDao(): FavouritesDao
//    abstract fun contactDao(): ContactInfoDao

    companion object {
        @Volatile
        private var INSTANCE: FavouritesDatabase? = null

        fun getDatabase(context: Context): FavouritesDatabase {
            val tempInstance = INSTANCE
            Log.d("DBG", "tempInstance = INSTANCE")

            if (tempInstance != null) {
                return tempInstance
            }

            Log.d("DBG", "tempInstance is not NULL")

            /*@Synchronized
            *
            * locks, only 1 instance of the database
            * runs on a seperate thread than main
            * */

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavouritesDatabase::class.java,
                    "favourites_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}