package com.example.artravel.wikipediaPlaces

data class PlaceInfoResponse(
    val name: String,
    val address: Address,
    val bbox: Bbox,
    val point: Point,
    val wikidata: String,
    val image: String,
    val preview: Preview?,
    val wikipedia_extracts: WikipediaExtracts
)
