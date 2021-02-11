package com.example.artravel.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artravel.activities.AttractionsDetailActivity
import com.example.artravel.attractionsRC.OnPlaceItemClickListener
import com.example.artravel.attractionsRC.Place
import com.example.artravel.attractionsRC.PlaceAdapter
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
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread


@Suppress("UNREACHABLE_CODE")
class AttractionsFragment : Fragment(), OnPlaceItemClickListener {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null
    private lateinit var placesList: ArrayList<Place>
    private lateinit var recyclerView: RecyclerView

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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_attractions, container, false)

        placesList = ArrayList()

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.addItemDecoration(DividerItemDecoration(activity, 1))
        recyclerView.layoutManager = LinearLayoutManager(view.context)

        recyclerView.adapter = PlaceAdapter(placesList, this)
        return view
    }


    override fun onItemClick(item: Place, position: Int) {
        val intent = Intent(activity, AttractionsDetailActivity::class.java)
        intent.putExtra("PLACENAME", item.name)
        intent.putExtra("PLACEIMAGE", item.image)
        intent.putExtra("PLACEDESC", item.desc)
        startActivity(intent)
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



        // Thread for network calls
        thread {
            for (dataResponse in dataResponses) {

                val newImage = getBitmapFromURL(dataResponse.preview?.source)

                placesList.add(
                    Place(
                        dataResponse.name,
                        newImage,
                        dataResponse.wikipedia_extracts?.text,
                        dataResponse.point?.lat,
                        dataResponse.point?.lon
                    )
                )

                Log.d(
                    "DBG", "${dataResponse.name},\n" +
                            "                        ${newImage},\n" +
                            "                        ${dataResponse.wikipedia_extracts?.text},\n" +
                            "                        ${dataResponse.point?.lat},\n" +
                            "                        ${dataResponse.point?.lon}"
                )
            }
            // Once for loop is finished, update places ArrayList and hide spinner.
            activity!!.runOnUiThread {
                recyclerView.adapter?.notifyDataSetChanged()
                hideProgressDialog()
            }
        }
    }

    private fun onFailure(t: Throwable) {
        Log.d("DBG", "Failure")
    }

    fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input: InputStream = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            // Log exception
            e.printStackTrace()
            null
        }
    }
}
