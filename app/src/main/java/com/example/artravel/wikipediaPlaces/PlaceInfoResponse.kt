package com.example.artravel.wikipediaPlaces

import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class PlaceInfoResponse(
    val name: String,
    val address: Address,
    val bbox: Bbox,
    val wikidata: String,
    val image: String,
    val preview: Preview,
    @SerialName("wikipedia_extracts") val wikipediaExtracts: WikipediaExtracts,
)
