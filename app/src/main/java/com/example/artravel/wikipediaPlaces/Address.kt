@file:Suppress("PLUGIN_IS_NOT_ENABLED")

package com.example.artravel.wikipediaPlaces
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Address(
    val city: String,
    val road: String,
    val house: String,
    val state: String,
    val county: String,
    val suburb: String,
    val country: String,
    val postcode: String,
    @SerialName("country_code") val countryCode: String,
    @SerialName("house_number") val houseNumber: String,
    @SerialName("city_district") val cityDistrict: String,
    val neighbourhood: String,
    @SerialName("state_district") val stateDistrict: String,
    )
