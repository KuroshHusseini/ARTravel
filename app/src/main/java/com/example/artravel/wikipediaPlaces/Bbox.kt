package com.example.artravel.wikipediaPlaces

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Bbox(
    @SerialName("lon_min") val lonMin: String,
    @SerialName("lon_max") val lonMax: String,
    @SerialName("lat_min") val latMin: String,
    @SerialName("lat_max") val latMax: String,
)
