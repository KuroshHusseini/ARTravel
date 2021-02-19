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

/*    @Query("SELECT * FROM user_table WHERE user_table.uid = :userid")
    // the @Relation do the INNER JOIN for you ;)
    fun getUserContacts(userid: Int): UserContact*/

}
