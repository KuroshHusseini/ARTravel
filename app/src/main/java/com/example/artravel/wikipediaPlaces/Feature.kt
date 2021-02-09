package com.example.artravel.wikipediaPlaces


data class Feature(
    val type: String,
    val id: String,
    val geometry: Geometry,
    val properties: Properties
)
