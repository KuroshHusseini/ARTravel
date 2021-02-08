package com.example.artravel.wikipediaPlaces

import java.io.Serializable

data class Query(
    val pages: List<Page>
) :Serializable