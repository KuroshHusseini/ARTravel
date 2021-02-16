package com.example.artravel.database

import androidx.lifecycle.LiveData


class PlaceRepository(private val placeDao: PlaceDao) {

    val readAllData: LiveData<List<DBPlace>> = placeDao.readAllData()

    suspend fun addPlace(place: DBPlace) {
        placeDao.addPlace(place)
    }

}