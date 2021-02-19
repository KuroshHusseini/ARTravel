package com.example.artravel.model.repository

import androidx.lifecycle.LiveData
import com.example.artravel.model.dao.AttractionDao
import com.example.artravel.model.entity.DBAttraction

class AttractionRepository(
    private val AttractionDao: AttractionDao
) {

    val readAllData: LiveData<List<DBAttraction>> = AttractionDao.readAllData()

    suspend fun addAttraction(attraction: DBAttraction) {
        AttractionDao.addAttraction(attraction)
    }

    suspend fun deleteAllAttractions() {
        AttractionDao.deleteAllAttractions()
    }
}