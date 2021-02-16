package com.example.artravel.database

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaceViewModel(application: Application) : AndroidViewModel(application) {

    private val readAllData: LiveData<List<DBPlace>>
    private val repository: PlaceRepository

    init {
        val placeDao = PlaceDatabase.getDatabase(application).placeDao()
        repository = PlaceRepository(placeDao)
        readAllData = repository.readAllData
    }

    fun addPlace(place: DBPlace) {
        //I want to run this code in the background thread
        viewModelScope.launch(Dispatchers.IO) {
            repository.addPlace(place)
        }
    }
}