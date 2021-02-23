package com.example.artravel.model.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.artravel.model.entity.DBPlace

@Dao
interface FavouritesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addFavourite(place: DBPlace)

    @Update
    suspend fun updateFavourite(place: DBPlace)

    @Delete
    suspend fun deleteFavourite(place: DBPlace)

    @Query("DELETE FROM place")
    suspend fun deleteAllFavourites()

    @Query("SELECT * FROM place ORDER BY id ASC")
    fun readAllData(): LiveData<List<DBPlace>>
}
