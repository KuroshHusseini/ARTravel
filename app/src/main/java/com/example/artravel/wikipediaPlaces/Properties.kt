package com.example.artravel.wikipediaPlaces

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class Properties(
    val xid: String,
    val name: String,
    val dist: Double,
    val rate: Int,
    val wikidata: String,
    val kinds: String
)
