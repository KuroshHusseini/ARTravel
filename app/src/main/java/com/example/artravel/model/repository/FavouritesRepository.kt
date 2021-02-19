package com.example.artravel.model.repository

import androidx.lifecycle.LiveData
import com.example.artravel.model.entity.DBPlace
import com.example.artravel.model.dao.FavouritesDao

class FavouritesRepository(
    private val FavouritesDao: FavouritesDao
) {

    val readAllData: LiveData<List<DBPlace>> = FavouritesDao.readAllData()

    suspend fun addUser(favourite: DBPlace) {
        FavouritesDao.addFavourite(favourite)
    }

    suspend fun updateUser(favourite: DBPlace) {
        FavouritesDao.updateFavourite(favourite)
    }

    suspend fun deleteUser(favourite: DBPlace) {
        FavouritesDao.deleteFavourite(favourite)
    }

    suspend fun deleteAllUsers() {
        FavouritesDao.deleteAllFavourites()
    }


}