package com.example.artravel.wikipediaPlaces

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

public interface MyBackendAPI {

    @GET("{xid}?")
    fun getPlaceInfo(
        @Path(value = "xid") xid: String,
        @Query("apikey") apikey: String,
    ): Observable<PlaceInfoResponse>

    @GET("radius?")
    fun getWikiArticles(
        @Query("radius") radius: Int,
        @Query("lon") lon: Double,
        @Query("lat") lat: Double,
        @Query("src_geom") src_geom: String,
        @Query("src_attr") src_attr: String,
        @Query("rate") rate: Int,
        @Query("limit") action: Int,
        @Query("apikey") appid: String?,
    ): Observable<WikipediaResponse>
}