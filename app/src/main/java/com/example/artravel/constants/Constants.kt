package com.example.artravel.constants

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object Constants {


    const val APP_ID: String = "f9ffb10ce3149b190a641828f7900651"
    const val WEATHER_URL: String = "https://api.openweathermap.org/data/"
    const val WIKIPEDIA_URL: String =
        "https://en.wikipedia.org/w/api.php?action=query&prop=coordinates|pageimages|description|info&inprop=url&pithumbsize=144&generator=geosearch&ggsradius=10000&ggslimit=10&ggscoord=61.2055|24.6559&format=json"
    const val METRIC_UNIT: String = "metric"


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