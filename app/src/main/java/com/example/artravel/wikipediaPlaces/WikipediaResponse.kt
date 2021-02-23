package com.example.artravel.wikipediaPlaces

import java.io.Serializable

data class WikipediaResponse(
    val type: String,
    val features: List<Feature>
)
