package com.example.artravel.model.entity

import android.graphics.Bitmap
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PLACE")
data class DBPlace(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var xid: String,
    var name: String,
    var image: Bitmap?,
    var desc: String?,
    var lat: String?,
    var lng: String?
)

@Entity(tableName = "ATTRACTION")
data class DBAttraction(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var xid: String,
    var name: String,
    var image: Bitmap?,
    var desc: String?,
    var lat: String?,
    var lng: String?
)
