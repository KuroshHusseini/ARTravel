package com.example.artravel.database

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface PlaceDao {

    @Query("SELECT * FROM place")
    fun readAllData(): LiveData<List<DBPlace>>

    @Query("SELECT * FROM place WHERE place.id = :placeId")
    suspend fun getPlaceById(placeId: Int): DBPlace

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPlace(DBPlace: DBPlace): Long

    @Update
    fun update(DBPlace: DBPlace)

    @Delete
    fun delete(DBPlace: DBPlace)

}