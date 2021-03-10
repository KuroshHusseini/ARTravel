package com.example.artravel.model.repository

import androidx.lifecycle.LiveData
import com.example.artravel.model.entity.DBPlace
import com.example.artravel.model.dao.FavouritesDao

class FavouritesRepository(
    FavouritesDao: FavouritesDao
) {
    val readAllData: LiveData<List<DBPlace>> = FavouritesDao.readAllData()
}