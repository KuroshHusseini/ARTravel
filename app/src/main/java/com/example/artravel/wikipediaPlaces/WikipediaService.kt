package com.example.artravel.wikipediaPlaces

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WikipediaService {

    @GET("api.php?")
    fun getWikiArticles(
        @Query("action") action: String,
        @Query("prop") prop: String,
        @Query("inprop") inprop: String,
        @Query("pithumbsize") pithumbsize: Int,
        @Query("generator") generator: String,
        @Query("ggsradius") ggsradius: Int,
        @Query("ggslimit") ggslimit: Int,
        @Query("ggscoord") ggscoord: String,
        @Query("format") format: String,
    ): Call<WikipediaResponse>
}