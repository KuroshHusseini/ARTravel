package com.example.artravel.favourites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.artravel.database.DBPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavouritesViewModel (application: Application) : AndroidViewModel(
    application
) {
    val readAllData: LiveData<List<DBPlace>>
    private val repository: FavouritesRepository

    init {
        val favouritesDao = FavouritesDatabase.getDatabase(application).favouriteDao()
        repository = FavouritesRepository(favouritesDao)
        readAllData = repository.readAllData
    }

    fun addFavourite(user: DBPlace) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addUser(user)
        }
    }

    fun updateFavourite(user: DBPlace) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateUser(user)
        }
    }

    fun deleteFavourite(user: DBPlace) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUser(user)
        }
    }

    fun deleteAllFavourites() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllUsers()
        }
    }
}