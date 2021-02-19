package com.example.artravel.fragments.list

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
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artravel.AttractionsRC.OnPlaceItemClickListener
import com.example.artravel.R
import com.example.artravel.constants.Constants
import com.example.artravel.model.database.ARTravelDatabase
import com.example.artravel.model.entity.DBAttraction
import com.example.artravel.model.viewmodel.AttractionViewModel
import com.example.artravel.wikipediaPlaces.*
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.attraction_item.*
import kotlinx.android.synthetic.main.fragment_attractions.*
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL


@Suppress("UNREACHABLE_CODE", "DEPRECATION")
class AttractionsFragment : Fragment(), OnPlaceItemClickListener {
    companion object {
        private val OPEN_TRIP_MAP_API_KEY = Constants.OPEN_TRIP_MAP_API_KEY
    }

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mProgressDialog: Dialog? = null

    //    private lateinit var placesList: ArrayList<DBAttraction>
    private val attractionsDatabase by lazy { ARTravelDatabase.getDatabase(requireContext()) }

    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    //What is this?
    inline fun <reified T> Gson.fromJson(json: String) = fromJson<T>(json, object : TypeToken<T>() {}.type)
    private fun sendNetworkRequests() {
        Log.d("Lifecycle", "sendNetworkRequests")
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

    override fun onDestroy() {
        super.onDestroy()
        if (disposable != null)
            disposable?.dispose()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_attractions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sendNetworkRequests()

        val ump = ViewModelProviders.of(this).get(AttractionViewModel::class.java)

        ump.readAllData.observe(this, {
            recycler_view.adapter = PlaceAdapter(
                requireContext(),
                it.sortedBy { that ->
                    that.name
                }, this
            )

            recycler_view.layoutManager = LinearLayoutManager(requireContext())
        })
    }

    private var disposable: Disposable? = null


    override fun onItemClick(item: Any, position: Int) {

        var item = item as DBAttraction

        var bundle = Bundle()

        bundle.putString("name", item.name)
        // Compress Bitmap as bytearray and uncompress in Detail Activity
        val stream = ByteArrayOutputStream()
        item.image?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val bytes: ByteArray = stream.toByteArray()
        bundle.putByteArray("bytes", bytes)
        if (item.desc != null) {
            bundle.putString("description", item.desc)
        }
        bundle.putString("lat", item.lat)
        bundle.putString("lon", item.lng)
        findNavController().navigate(
            R.id.action_attractionsFragment_to_attractionsDetailFragment,
            bundle
        )
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
            Log.d("PERKELE!", "$latitude $longitude")
            showCustomProgressDialog()
            disposable =
                ServiceBuilder.buildService()
                    .getWikiArticles(
                        2000,
                        longitude,
                        latitude,
                        "wikidata",
                        "wikidata",
                        0,
                        5,
                        OPEN_TRIP_MAP_API_KEY
                    )
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.newThread())
                    .subscribe({ response ->
                        disposable?.dispose()

                        onResponse(response)
                    },
                        { t ->
                            onFailure(t)

                            disposable?.dispose()
                        })
        }
    }


    private fun onResponse(response: WikipediaResponse) {
        // continue working and dispose all subscriptions when the values from the Single objects are not interesting any more
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
                    OPEN_TRIP_MAP_API_KEY
                )
            )
        }
        disposable = Observable.zip(requests) { objects ->
            val dataResponses = mutableListOf<PlaceInfoResponse>()
            for (o in objects) {
                val placeInfo = o as PlaceInfoResponse
                dataResponses.add(placeInfo)
            }
            return@zip dataResponses
        }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.newThread())
            .subscribe({ placeInfos ->
                Log.d("DBG", "Success")
                disposable?.dispose()
                setupUI(placeInfos)
            }, { t ->
                t.printStackTrace()
                Log.d("DBG", "Failure")
                disposable?.dispose()
                activity?.runOnUiThread {
                    hideProgressDialog()
                }
            })
    }
    // extension function to get / download bitmap from url
    private fun URL.toBitmap(): Bitmap? {
        return try {
            BitmapFactory.decodeStream(openStream())
        } catch (e: IOException) {
            null
        }
    }
    private suspend fun updateUi(dataResponses: MutableList<PlaceInfoResponse>) {


        GlobalScope.launch {
            attractionsDatabase.attractionDao()
                .deleteAllAttractions()
        }

        GlobalScope.async {

            for (dataResponse in dataResponses) {
                val url: URL = if (dataResponse.preview?.source == null) {
                    URL("https://cdn-a.william-reed.com/var/wrbm_gb_food_pharma/storage/images/9/2/8/5/235829-6-eng-GB/Feed-Test-SIC-Feed-20142_news_large.jpg")
                } else {
                    URL(dataResponse.preview.source)
                }
                val result: Deferred<Bitmap?> = GlobalScope.async {
                    url.toBitmap()
                }
                val bitmap: Bitmap? = result.await()
                Log.d(
                    "DEBUG",
                    "${dataResponse.name}: ${dataResponse.point.lat} ${dataResponse.point.lon}"
                )


                GlobalScope.launch {
                    attractionsDatabase.attractionDao()
                        .addAttraction(
                            DBAttraction(
                                0,
                                dataResponse.name,
                                bitmap,
                                dataResponse?.wikipedia_extracts?.text,
                                dataResponse.point.lat,
                                dataResponse.point.lon
                            )
                        )
                }


//                placesList.add(
//                    DBAttraction(
//                        0,
//                        dataResponse.name,
//                        bitmap,
//                        dataResponse?.wikipedia_extracts?.text,
//                        dataResponse.point.lat,
//                        dataResponse.point.lon
//                    )
//                )

                Log.d(
                    "DEBUG", "${dataResponse.name},\n" +
                            "${bitmap},\n" +
                            "${dataResponse?.wikipedia_extracts?.text},\n" +
                            "${dataResponse.point.lat},\n" +
                            dataResponse.point.lon
                )
//
//                recycler_view.adapter?.notifyDataSetChanged()
//
//                hideProgressDialog()
            }
        }

        hideProgressDialog()
    }

    private fun setupUI(dataResponses: MutableList<PlaceInfoResponse>) {
        GlobalScope.launch(Dispatchers.Main) {
            updateUi(dataResponses)
        }
    }
    private fun onFailure(t: Throwable) {
        t.printStackTrace()
        Log.d("DBG", "Failure")
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.favorites_places_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    // when button is pressed do this
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorites -> {
                findNavController().navigate(R.id.action_attractionsFragment_to_favouritesFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
