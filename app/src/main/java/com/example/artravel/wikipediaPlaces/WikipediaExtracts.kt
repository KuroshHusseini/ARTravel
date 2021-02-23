package com.example.artravel.wikipediaPlaces

import kotlinx.serialization.Serializable

@Suppress("PLUGIN_IS_NOT_ENABLED")
@Serializable
data class WikipediaExtracts(
    val title: String,
    val text: String,
    val html: String,
)
