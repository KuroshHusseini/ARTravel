package com.example.artravel.database

import android.graphics.Bitmap
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PLACE")
data class DBPlace(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var name: String,
    var image: Bitmap?,
    var desc: String?,
    var lat: String?,
    var lng: String?

)
