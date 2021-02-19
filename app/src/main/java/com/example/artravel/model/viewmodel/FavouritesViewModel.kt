package com.example.artravel.model.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.artravel.model.entity.DBPlace
import com.example.artravel.model.database.ARTravelDatabase
import com.example.artravel.model.repository.FavouritesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavouritesViewModel (application: Application) : AndroidViewModel(
    application
) {
    val readAllData: LiveData<List<DBPlace>>
    val repository: FavouritesRepository

    init {

        val favouriteDao = ARTravelDatabase.getDatabase(application).favouriteDao()
        repository = FavouritesRepository(favouriteDao)
        readAllData = repository.readAllData
    }
}