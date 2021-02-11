package com.example.artravel

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.directions.route.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import java.util.*


class AttractionsDrawRouteActivity : AppCompatActivity(), OnMapReadyCallback, RoutingListener {
    private lateinit var map: GoogleMap

    private val LOCATION_PERMISSION_REQUEST = 1

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // Destination LatLng
    private var destinationLat: String? = null
    private var destinationLng: String? = null


    // Destination
    //             val dataResponses = mutableListOf<PlaceInfoResponse>()
    private val polylines = mutableListOf<Polyline>()
//    private val polylines: musta<Polyline>? = null

    private var userLocation: Location? = null

    private var start: LatLng? = null
    private var end: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attractions_draw_route)

        destinationLat = intent.getStringExtra("destinationLatitude")

        destinationLng = intent.getStringExtra("destinationLongitude")

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // For locating and updating user location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        // Setup back button

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        getLocationAccess()
    }

    private fun getLocationAccess() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            getLocationUpdates()

            startLocationUpdates()

            Toast.makeText(
                this,
                R.string.user_granted_permission,
                Toast.LENGTH_LONG
            )
                .show()

        } else
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
    }

    private fun setDestination() {

        userLocation ?: return

        start = LatLng(userLocation!!.latitude, userLocation!!.longitude)

        end = LatLng(destinationLat!!.toDouble(), destinationLng!!.toDouble())

        findRoute(start, end)
    }

    private fun findRoute(start: LatLng?, end: LatLng?) {
        if (start == null || end == null) {
            Toast.makeText(this, "Unable to get location", Toast.LENGTH_LONG).show()
        } else {

            var routing: Routing? = Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(start, end)
                .key(getString(R.string.google_maps_key))
                .build()
            routing!!.execute()

        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    private fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 30000
        locationRequest.fastestInterval = 20000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {

                if (locationResult.locations.isNotEmpty()) {
                    userLocation = locationResult.lastLocation

                    if (userLocation != null) {
                        val latLng = LatLng(userLocation!!.latitude, userLocation!!.longitude)

                        map.addMarker(
                            MarkerOptions().position(latLng).title("You are here.")
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(
                                        resizeMapIcons(
                                            "map_marker",
                                            100,
                                            100
                                        )
                                    )
                                )
                        )

                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                        setDestination()
                    }
                }

            }
        }

    }

    fun resizeMapIcons(iconName: String?, width: Int, height: Int): Bitmap? {
        val imageBitmap = BitmapFactory.decodeResource(
            resources,
            resources.getIdentifier(iconName, "drawable", packageName)
        )
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    }

    override fun onRoutingFailure(e: RouteException?) {
        val parentLayout: View = findViewById(android.R.id.content)
        val snackbar: Snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    override fun onRoutingStart() {
        Toast.makeText(this, "Finding Route...", Toast.LENGTH_LONG).show();
    }

    override fun onRoutingSuccess(route: ArrayList<Route>?, shortestRouteIndex: Int) {
//        var center: CameraUpdate = CameraUpdateFactory.newLatLngZoom(start)
//        var zoom: CameraUpdate = CameraUpdateFactory.zoomTo(16f)

        polylines.clear()

        var polyOptions = PolylineOptions()
        var polyLineStartLatLng: LatLng? = null
        var polylineEndLatLng:LatLng? = null

        for (i in 0 until route!!.size) {

            if (i == shortestRouteIndex) {
                polyOptions.color(resources.getColor(R.color.colorPrimary))
                polyOptions.width(7f)
                polyOptions.addAll(route[shortestRouteIndex].points)
                var polyline = map.addPolyline(polyOptions)
                polyLineStartLatLng = polyline.points[0]

                var k: Int = polyline.points.size

                polylineEndLatLng = polyline.points[k-1]

                polylines.add(polyline)
            }


            // Add Marker on route starting position
            var startMarker = MarkerOptions()
            if (polyLineStartLatLng != null) {
                startMarker.position(polyLineStartLatLng)
            }
            startMarker.title("My Location")
            map.addMarker(startMarker)

            // Add Marker on route ending position
            var endMarker = MarkerOptions()
            if (polylineEndLatLng != null) {
                endMarker.position(polylineEndLatLng)
            }
            endMarker.title("Destination")
            map.addMarker(endMarker)
        }
    }

    override fun onRoutingCancelled() {
        findRoute(start, end);
    }
}