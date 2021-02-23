package com.example.artravel.wikipediaPlaces

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Suppress("PLUGIN_IS_NOT_ENABLED")
@Serializable
data class Point(
    val lon: String,
    val lat: String
)
