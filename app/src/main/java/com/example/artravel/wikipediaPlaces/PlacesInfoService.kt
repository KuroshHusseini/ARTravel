package com.example.artravel.wikipediaPlaces

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PlacesInfoService {
    @GET("{xid}?")
    fun getPlaceInfo(
        @Path(value = "xid") xid: String,
        @Query("apikey") apikey: String,
    ): Call<PlaceInfoResponse>
}
