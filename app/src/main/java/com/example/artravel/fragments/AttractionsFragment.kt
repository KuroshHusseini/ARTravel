package com.example.artravel.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.artravel.R
import com.example.artravel.constants.Constants
import com.example.artravel.wikipediaPlaces.*
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class AttractionsFragment : Fragment() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        return inflater.inflate(R.layout.fragment_attractions, container, false)
    }

    private fun isLocationEnable(): Boolean {
        //provides access to the system location services.
        val locationManager: LocationManager =
            activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
                        // show alert dialog navigating to Settings
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

    //getting the location
    @SuppressLint("MissingPermission")
    private fun requestLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun showRationalDialogForPermission() {
        AlertDialog.Builder(activity)
            .setMessage(getString(R.string.show_rational_dialog_for_permission_alert_txt))
            .setPositiveButton(getString(R.string.positive_button_for_alert_txt)) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", activity!!.packageName, null)
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

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("Current Latitude", "$latitude")

            val longitude = mLastLocation.longitude
            Log.i("Current longitude", "$longitude")
            getNearbyPlaces(latitude, longitude)
        }
    }

    //custom dialog
    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(activity!!)
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    private fun getNearbyPlaces(latitude: Double, longitude: Double) {

        if (Constants.isNetworkAvailable(activity)) {
            val compositeDisposable = CompositeDisposable()

            showCustomProgressDialog()

            compositeDisposable.add(
                ServiceBuilder.buildService()
                    .getWikiArticles(
                        2000,
                        longitude,
                        latitude,
                        "wikidata",
                        "wikidata",
                        0,
                        10,
                        "5ae2e3f221c38a28845f05b63f384c730e6c086fbc1c4ea103a5c463"
                    )
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.newThread())
                    .subscribe({ response ->
                        hideProgressDialog()
                        onResponse(response)
                    },
                        { t ->
                            onFailure(t)
                        })
            )
        }
    }


    private fun onResponse(response: WikipediaResponse) {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.opentripmap.com/0.1/en/places/xid/")
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val backendAPI = retrofit.create(MyBackendAPI::class.java)

        val requests = ArrayList<Observable<PlaceInfoResponse>>()

        for (i in response.features.indices) {
            Log.d("DBG", response.features[i].properties.xid)

            requests.add(
                backendAPI.getPlaceInfo(
                    response.features[i].properties.xid,
                    "5ae2e3f221c38a28845f05b63f384c730e6c086fbc1c4ea103a5c463"
                )
            )
        }

        Log.d("DBG", "Size: ${requests.size}")

        Observable.zip(requests) { objects ->

            val dataResponses = mutableListOf<PlaceInfoResponse>()

            for (o in objects) {

                var placeInfo = o as PlaceInfoResponse

                dataResponses.add(placeInfo)
            }


            return@zip dataResponses
        }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe({ placeInfos ->
                Log.d("DBG", "Success")
                setupUI(placeInfos)

            }, { t ->
                Log.d("DBG", "Failure")
            })

    }

    private fun setupUI(dataResponses: MutableList<PlaceInfoResponse>) {

        /*
        *
        * TODO: Setup data for RecyclerView
        *
        * */

        Log.d("DBG", "${dataResponses}")
    }

    private fun onFailure(t: Throwable) {
        Log.d("DBG", "Failure")
    }


}
