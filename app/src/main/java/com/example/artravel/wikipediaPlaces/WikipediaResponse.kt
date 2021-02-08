package com.example.artravel.wikipediaPlaces

import java.io.Serializable

data class WikipediaResponse(
    val batchcomplete: String,
    val query: Query,
//    val query: List<Query>

/*    val weather: Query,*/

) : Serializable
