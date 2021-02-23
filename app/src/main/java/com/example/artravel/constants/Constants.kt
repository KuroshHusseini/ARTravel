@file:Suppress("DEPRECATION")

package com.example.artravel.constants

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.example.artravel.BuildConfig


/**
 * Checks for permissions such as: Network,Version, WIFI, Internet
 *
 * @author Kurosh Husseini
 * @date 23.02.2021
 */

object Constants {
    const val OPEN_WEATHER_API_KEY: String = BuildConfig.OPEN_WEATHER_API_KEY
    const val OPEN_TRIP_MAP_API_KEY = BuildConfig.OPEN_TRIP_MAP_API_KEY
    const val GOOGLE_API_KEY = BuildConfig.GOOGLE_API_KEY

    const val WEATHER_URL: String = "https://api.openweathermap.org/data/"
    const val METRIC_UNIT: String = "metric"

    @SuppressLint("ObsoleteSdkInt")
    fun isNetworkAvailable(context: Context?): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // checking for the version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnectedOrConnecting
        }
    }
}