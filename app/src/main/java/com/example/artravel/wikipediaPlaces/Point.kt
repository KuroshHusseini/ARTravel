package com.example.artravel.wikipediaPlaces

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Point(
    val lon: String,
    val lat: String
)
