package com.example.artravel.model.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.artravel.model.entity.DBAttraction

@Dao
interface AttractionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addAttraction(attraction: DBAttraction)

    @Query("DELETE FROm attraction")
    suspend fun deleteAllAttractions()

    @Query("SELECT * FROM attraction ORDER BY id ASC")
    fun readAllData(): LiveData<List<DBAttraction>>

}