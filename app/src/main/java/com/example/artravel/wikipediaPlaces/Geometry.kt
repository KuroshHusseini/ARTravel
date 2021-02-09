package com.example.artravel.wikipediaPlaces

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class Geometry(
    val type: String,
    val coordinates: List<Double>
)
