package com.example.artravel.attractionsRC

import android.graphics.Bitmap

data class Place(
    var name: String,
    var image: Bitmap?,
    var desc: String?,
    var lat: String?,
    var lng: String?
)
