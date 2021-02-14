package com.example.artravel

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.directions.route.*
import com.example.artravel.constants.Constants
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import java.util.*

class AttractionsDrawRoute : Fragment(), RoutingListener {

    val GOOGLE_API_KEY = Constants.GOOGLE_API_KEY

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

    private var parentLayout: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        destinationLat = arguments?.getString("lat")
        destinationLng = arguments?.getString("lon")


        destinationLat?.let { Log.d("WTF", it) }

        destinationLng?.let { Log.d("WTF", it) }

//        if (lat != null) {
//            Log.d("DrawRoute", lat)
//        }
//        if (lon != null) {
//            Log.d("DrawRoute", lon)
//        }

        parentLayout = view?.findViewById(android.R.id.content)

        return inflater.inflate(R.layout.fragment_attractions_draw_route, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.google_map_draw_route) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        // For locating and updating user location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        map = googleMap
        getLocationAccess()

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        this.map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            // Return null here, so that getInfoContents() is called next.
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                // Inflate the layouts for the info window, title and snippet.
                val infoWindow = layoutInflater.inflate(
                    R.layout.custom_info_contents,
                    view?.findViewById<FrameLayout>(R.id.google_map), false
                )
                val title = infoWindow.findViewById<TextView>(R.id.title)
                title.text = marker.title
                val snippet = infoWindow.findViewById<TextView>(R.id.snippet)
                snippet.text = marker.snippet
                return infoWindow
            }
        })
    }

    private fun getLocationAccess() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            getLocationUpdates()
            startLocationUpdates()
            Toast.makeText(
                view?.context,
                R.string.user_granted_permission,
                Toast.LENGTH_LONG
            )
                .show()

        } else
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    view?.context,
                    R.string.user_not_grant_permission,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = 30000
        locationRequest.fastestInterval = 20000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                // Geocode user location address
//                val addresses: List<Address>

                if (locationResult.locations.isNotEmpty()) {
                    userLocation = locationResult.lastLocation

                    if (userLocation != null) {


                        activity ?: return
//                val geocoder = Geocoder(activity!!.application, Locale.getDefault())

                        val latLng = LatLng(userLocation!!.latitude, userLocation!!.longitude)

//                        addresses =
//                            geocoder.getFromLocation(location.latitude, location.longitude, 1)

//                        val address: String = addresses[0].getAddressLine(0)

//                        Log.d("DBG", address)

                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

                        setDestination()
                    }
                }
            }
        }
    }

    private fun setDestination() {

        userLocation ?: return

        start = LatLng(userLocation!!.latitude, userLocation!!.longitude)

        end = LatLng(destinationLat!!.toDouble(), destinationLng!!.toDouble())

        findRoute(start, end)
    }

    private fun findRoute(start: LatLng?, end: LatLng?) {
        if (start == null || end == null) {
            Toast.makeText(activity, "Unable to get location", Toast.LENGTH_LONG).show()
        } else {

            var routing: Routing? = Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(start, end)
                .key(GOOGLE_API_KEY)
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

    override fun onRoutingFailure(e: RouteException?) {
//        val parentLayout: View = view?.rootView?.findViewById(android.R.id.content) ?:

//        val parentLayout: View = fragmentManager?.findView(android.R.id.content)

        if (parentLayout != null) {
            val snackbar: Snackbar = Snackbar.make(parentLayout!!, e.toString(), Snackbar.LENGTH_LONG)
            snackbar.show()

        } else {
            Toast.makeText(activity, "Routing failed.", Toast.LENGTH_LONG).show();

        }

    }

    override fun onRoutingStart() {
        Toast.makeText(activity, "Finding Route...", Toast.LENGTH_LONG).show();
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
            var startMarker: MarkerOptions? = MarkerOptions()

            if (polyLineStartLatLng != null) {
                startMarker?.position(polyLineStartLatLng)
            }
            startMarker?.title("My Location")
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