package com.example.artravel.database

import android.graphics.Bitmap
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PLACE")
data class RoomPlace(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var dbName: String,
    var dbImage: Bitmap?,
    var DBDesc: String?,
    var DBLat: String?,
    var DBLng: String?

)
