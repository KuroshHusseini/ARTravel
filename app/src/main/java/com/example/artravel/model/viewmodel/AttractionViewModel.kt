package com.example.artravel.model.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.artravel.model.database.ARTravelDatabase
import com.example.artravel.model.entity.DBAttraction
import com.example.artravel.model.repository.AttractionRepository


class AttractionViewModel(application: Application): AndroidViewModel(
    application
){

    val readAllData: LiveData<List<DBAttraction>>
    val repository: AttractionRepository

    init {
        val attractionDao = ARTravelDatabase.getDatabase(application).attractionDao()
        repository = AttractionRepository(attractionDao)
        readAllData = repository.readAllData
    }
}