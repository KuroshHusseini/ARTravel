package com.example.artravel.model.repository

import androidx.lifecycle.LiveData
import com.example.artravel.model.dao.AttractionDao
import com.example.artravel.model.entity.DBAttraction

class AttractionRepository(
    AttractionDao: AttractionDao
) {
    val readAllData: LiveData<List<DBAttraction>> = AttractionDao.readAllData()
}