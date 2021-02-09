package com.example.artravel.wikipediaPlaces
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WikipediaExtracts(
    val title: String,
    val text: String,
)
