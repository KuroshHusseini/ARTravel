package com.example.artravel.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.artravel.R
import com.example.artravel.constants.Constants
import com.example.artravel.weatherModels.WeatherResponse
import com.example.artravel.weatherNetwork.WeatherService
import com.google.android.gms.location.*

import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.fragment_weather.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * WeatherFragment uses OpenWeatherAPI Services to show current weather
 *
 * @author Kurosh Husseini
 * @date 23.02.2021
 */

@Suppress("DEPRECATION", "NAME_SHADOWING", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class WeatherFragment : Fragment() {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mFusedLocationClient =
            activity?.let { LocationServices.getFusedLocationProviderClient(it) }!!
        //Checking if location is enabled
        if (!isLocationEnable()) {
            Toast.makeText(
                activity,
                getString(R.string.toast_txt_for_location),
                Toast.LENGTH_SHORT
            ).show()
            //redirect the user to the setting to turn on the location
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            requestMultiplePermissions()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity ?: return

//        val sharedPreferences = SharedPreferences.ge

        val sharedPreferences = activity?.getSharedPreferences("night", 0)

        val booleanValue: Boolean = sharedPreferences?.getBoolean("night_mode", true) == true

        if (booleanValue) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            switch_darkLight.isChecked = true
        }

        switch_darkLight.setOnClickListener {

            val isNightTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

            // 4
            when (isNightTheme) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_NO
                    )
                }
                Configuration.UI_MODE_NIGHT_NO -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )
            }
        }
    }

    /**
     * @param dataResponses array with properties (e.g. coord, weather, base, main, id , name, cod etc).
     * Based on users location
     *
     *  @author Kurosh Husseini
     * @date 23.02.2021
     */
    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) {
        if (Constants.isNetworkAvailable(activity)) {
            val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl(Constants.WEATHER_URL)
                .addConverterFactory(GsonConverterFactory.create()).build()

            val service: WeatherService =
                retrofit.create(WeatherService::class.java)

            val listCall: Call<WeatherResponse> = service.getWeather(
                latitude, longitude, Constants.METRIC_UNIT, Constants.OPEN_WEATHER_API_KEY
            )
            showCustomProgressDialog()
            listCall.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response.isSuccessful) {
                        //if response is successful hide the progress bar and show the data
                        hideProgressDialog()
                        val weatherList: WeatherResponse? = response.body()
                        setupUI(weatherList!!)
                        Log.i("Response Result", "$weatherList")
                    } else {
                        when (response.code()) {
                            400 -> {
                                Log.e("Error 300", "Bad connection")
                            }
                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                            else -> {
                                Log.e("Error", "Generic Error")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    Log.e("Error", t.message.toString())
                    hideProgressDialog()
                }
            })
        } else {
            Toast.makeText(
                activity,
                getString(R.string.You_have_not_been_connected_to_internet),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun isLocationEnable(): Boolean {
        val locationManager: LocationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestMultiplePermissions() {
        Dexter.withActivity(activity)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        requestLocationData()
                    }
                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        showRationalDialogForPermission()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    showRationalDialogForPermission()
                }
            })
            .onSameThread()
            .check()
    }

    private fun showRationalDialogForPermission() {
        AlertDialog.Builder(activity)
            .setMessage(getString(R.string.show_rational_dialog_for_permission_alert_txt))
            .setPositiveButton(getString(R.string.positive_button_for_alert_txt)) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package",requireActivity().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton(getString(R.string.negative_button_for_alert_txt)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    //Request location data
    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient.removeLocationUpdates(mLocationCallback)

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }


    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("Current Latitude", "$latitude")

            val longitude = mLastLocation.longitude
            Log.i("Current longitude", "$longitude")
            getLocationWeatherDetails(latitude, longitude)
        }
    }


    //Setting UI texts
    @SuppressLint("SetTextI18n")
    private fun setupUI(weatherList: WeatherResponse) {
        for (i in weatherList.weather.indices) {
            Log.i("Weather Name", weatherList.weather.toString())

            tv_main?.text = weatherList.weather[i].main
            tv_main_description?.text = weatherList.weather[i].description
            tv_temp?.text =
                "${weatherList.main.temp} ${getUnit(resources.configuration.toString())}"

            id_sunrise_txt.text = getString(R.string.sunrise_txt)
            id_sunset_txt.text = getString(R.string.sunset_txt)
            tv_sunrise_time?.text = unixTime(weatherList.sys.sunrise)
            tv_sunset_time?.text = unixTime(weatherList.sys.sunset)

            tv_humidity?.text = "${weatherList.main.humidity} %"

            tv_min?.text = "${weatherList.main.temp_min} min"
            tv_max?.text = "${weatherList.main.temp_max} max"

            tv_speed?.text = weatherList.wind.speed.toString()
            tv_name?.text = weatherList.name
            tv_country?.text = weatherList.sys.country

            when (weatherList.weather[i].icon) {
                "01d" -> iv_main?.setImageResource(R.drawable.sunny)
                "02d" -> iv_main?.setImageResource(R.drawable.cloud)
                "03d" -> iv_main?.setImageResource(R.drawable.cloud)
                "04d" -> iv_main?.setImageResource(R.drawable.cloud)
                "09d" -> iv_main?.setImageResource(R.drawable.rain)
                "10d" -> iv_main?.setImageResource(R.drawable.rain)
                "11d" -> iv_main?.setImageResource(R.drawable.storm)
                "13d" -> iv_main?.setImageResource(R.drawable.snowflake)
            }
        }
    }

    //get the right unit
    private fun getUnit(value: String): String {
        var vl = "°C"
        if ("US" == value || "LR" == value || "MM" == value) {
            vl = "°F"
        }
        return vl
    }

    // getting time
    private fun unixTime(timex: Long): String {
        val date = Date(timex * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.UK)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    //custom dialog
    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(requireActivity())
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.weather_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // Refresh button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val refreshButton = R.id.action_refresh
        return when (item.itemId) {
            refreshButton -> {
                requestLocationData()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}