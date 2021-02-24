package com.example.artravel.fragments

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
import com.example.artravel.R
import com.example.artravel.constants.Constants
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import java.util.*

/**
 * Fragment for drawing a route from user to destination.
 *
 * @author Michael Lock
 * @date 23.02.2021
 */

@Suppress("DEPRECATION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AttractionsDrawRoute : Fragment(), RoutingListener {
    companion object {
        val GOOGLE_API_KEY = Constants.GOOGLE_API_KEY
        private const val LOCATION_PERMISSION_REQUEST = 1
    }

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // Destination LatLng
    private var destinationLat: String? = null
    private var destinationLng: String? = null

    // Destination
    private val polylines = mutableListOf<Polyline>()

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

        parentLayout = view?.findViewById(android.R.id.content)
        return inflater.inflate(R.layout.fragment_attractions_draw_route, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("END", "onViewCreated")
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.google_map_draw_route) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        // For locating and updating user location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        getLocationAccess()
        // Use a custom info window adapter to handle multiple lines of text in the

        Log.d("END", "Does map exist? ${map.toString()}")
        Log.d("END", "Does map exist?")
        // info window contents.
        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
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

    /**
     * Calls methods that locate user and request frequent location updates
     *
     * This method is called when google maps is ready.
     * @author Michael Lock
     * @date 23.02.2021
     */
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

    /**
     * getLocationUpdates method requests user location every 3 seconds and
     * updates the drawn route from user to destination.
     *
     * This method is called when user has accepted the location permissions
     * @author Michael Lock
     * @date 23.02.2021
     */

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
                        activity ?: return

                        val latLng = LatLng(userLocation!!.latitude, userLocation!!.longitude)

                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                        setDestination()
                    }
                }
            }
        }
    }

    /**
     * setDestination method sets route to destination.
     *
     * This method is called when user location has been found.
     * @author Michael Lock
     * @date 23.02.2021
     */

    private fun setDestination() {
        userLocation ?: return
        start = LatLng(userLocation!!.latitude, userLocation!!.longitude)
        end = LatLng(destinationLat!!.toDouble(), destinationLng!!.toDouble())


        Log.d("END", "$start")
        Log.d("END", "$end")
        findRoute(start, end)
    }

    private fun findRoute(start: LatLng?, end: LatLng?) {
        if (start == null || end == null) {
            Log.d("END", "Unable to get location")

            Toast.makeText(activity, "Unable to get location", Toast.LENGTH_LONG).show()
        } else {

            Log.d("END", "var routing=Routing")
            val routing: Routing? = Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(start, end)
                .key(GOOGLE_API_KEY)
                .build()
            routing!!.execute()

        }
    }

    /**
     * startLocationUpdates method attaches client to Google Play Services for location updates.
     *
     * This method is called when user accepted location permissions.
     * @author Michael Lock
     * @date 23.02.2021
     */

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    override fun onRoutingFailure(e: RouteException?) {
        if (parentLayout != null) {
            val snackbar: Snackbar =
                Snackbar.make(parentLayout!!, e.toString(), Snackbar.LENGTH_LONG)
            snackbar.show()
        } else {
            Toast.makeText(activity, getString(R.string.routing_failed), Toast.LENGTH_LONG).show()
        }
    }

    override fun onRoutingStart() {
        Log.d("END", "Finding route...")
        Toast.makeText(activity, getString(R.string.finding_routing), Toast.LENGTH_LONG).show()
    }

    override fun onRoutingSuccess(route: ArrayList<Route>?, shortestRouteIndex: Int) {
        polylines.clear()
        val polyOptions = PolylineOptions()
        var polyLineStartLatLng: LatLng? = null
        var polylineEndLatLng: LatLng? = null
        for (i in 0 until route!!.size) {

            if (i == shortestRouteIndex) {
                polyOptions.color(resources.getColor(R.color.colorPrimary))
                polyOptions.width(7f)
                polyOptions.addAll(route[shortestRouteIndex].points)

                val polyline = map.addPolyline(polyOptions)

                polyLineStartLatLng = polyline.points[0]
                val k: Int = polyline.points.size

                polylineEndLatLng = polyline.points[k - 1]
                polylines.add(polyline)

            }

            Log.d("polylineStart", polyLineStartLatLng.toString())
            Log.d("polylineEmd", polylineEndLatLng.toString())

            // Add Marker on route starting position

            if (polyLineStartLatLng == null) {
                Toast.makeText(
                    requireContext(),
                    "Could not draw route to destination.",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }

            val startMarker = MarkerOptions()
            startMarker.position(polyLineStartLatLng!!)
            startMarker.title("My Location")
            map.addMarker(startMarker)
            // Add Marker on route ending position
            val endMarker = MarkerOptions()
            endMarker.position(polylineEndLatLng!!)
            endMarker.title("Destination")
            map.addMarker(endMarker)
        }
    }

    override fun onRoutingCancelled() {
        findRoute(start, end)
    }
}