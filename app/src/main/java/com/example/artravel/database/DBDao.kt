package com.example.artravel.database

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface DBDao {

    @Query("SELECT * FROM place")
    fun getAll(): LiveData<List<RoomPlace>>

    @Query("SELECT * FROM place WHERE place.id = :placeId")
    suspend fun getPlaceById(placeId: Int): RoomPlace

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(place: RoomPlace): Long

    @Update
    fun update(place: RoomPlace)

    @Delete
    fun delete(place: RoomPlace)

}