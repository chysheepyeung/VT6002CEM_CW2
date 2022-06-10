package com.example.traditionalarchitecturemaintain

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class DirectionActivity : AppCompatActivity() {


    lateinit var googleMap: GoogleMap
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private var locationPermissionGranted = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private val DEFAULT_ZOOM = 15
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private val KEY_CAMERA_POSITION = "camera_position"
    private val KEY_LOCATION = "location"
    private var cameraPosition: CameraPosition? = null
    var destination:LatLng? = null
    var origin:LatLng? = null
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_direction)
        val location = intent.getStringExtra("location")


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        getLocationPermission()

        GlobalScope.launch(Dispatchers.IO) {
            Log.d("GoogleMap", "before getDeviceLocation")
            if (locationPermissionGranted) {
                Log.d("GoogleMap", "location permission grant.")
                val locationResult = fusedLocationProviderClient.lastLocation.await()

                Log.d("GoogleMap map", "location Result success.")
                // Set the map's camera position to the current location of the device.
                lastKnownLocation = locationResult

                val getLocationUrl = getDestinationLocationURL(location!!)
                GetDestination(getLocationUrl).execute()
                withContext(Dispatchers.Main) {

                    val mapFragment =
                        supportFragmentManager.findFragmentById(R.id.direct_map) as SupportMapFragment?
                    mapFragment?.getMapAsync(OnMapReadyCallback {
                        googleMap = it
                        Log.d("GoogleMap", "before isMyLocationEnabled")
                        Thread.sleep(2000)
                        Log.d(
                            "GoogleMap map",
                            "lastKnownLocation ${lastKnownLocation!!.latitude}, ${lastKnownLocation!!.longitude}"
                        )
                        if (lastKnownLocation != null) {
                            googleMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                        } else {
                            Log.d("GoogleMap", "Current location is null. Using defaults.")
                            googleMap.moveCamera(
                                CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                            )
                            googleMap.uiSettings.isMyLocationButtonEnabled = false
                        }

                        googleMap.isMyLocationEnabled = true

                        Log.d("GoogleMap", "before origin")
                        origin =
                            LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                        googleMap.addMarker(MarkerOptions().position(origin!!).title("My Location"))
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(origin,5f))


                        Log.d("GoogleMap", "before dest")

                        //destination = LatLng(22.271983650005588, 114.2397062684834)


                    })
                }
            }
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MapsActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        googleMap.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            MapsActivity.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun getDirectionURL(origin:LatLng, dest:LatLng) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyCfjmmfDobKm_PDpCGHNC7dUnjje1e3Qns"
    }

    private fun getDestinationLocationURL(location: String):String{
        var address = location.replace(" ", "%20")
        return "https://maps.google.com/maps/api/geocode/json?address=$address&sensor=false&key=AIzaSyCfjmmfDobKm_PDpCGHNC7dUnjje1e3Qns"
    }

    private inner class GetDestination(val url : String) : AsyncTask<Void,Void,LatLng?>(){
        override fun doInBackground(vararg params: Void?): LatLng? {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()!!.string()
            Log.d("GoogleMap" , " destination data : $data")
            var result:LatLng? = null
            try{
                val respObj = Gson().fromJson(data,DestinationDTO::class.java)


                var destination = respObj.results[0].geometry.location
                result = LatLng(destination.lat.toDouble(), destination.lng.toDouble())
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: LatLng?) {
            destination = result
            googleMap.addMarker(
                MarkerOptions().position(destination!!).title("Destination")
            )

            Log.d("GoogleMap", "before URL")
            val URL = getDirectionURL(origin!!, destination!!)
            Log.d("GoogleMap", "URL : $URL")
            GetDirection(URL).execute()
        }
    }

    private inner class GetDirection(val url : String) : AsyncTask<Void,Void,List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()!!.string()
            Log.d("GoogleMap" , " data : $data")
            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data,GoogleMapDTO::class.java)

                val path =  ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size-1)){
//                    val startLatLng = LatLng(respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
//                    path.add(startLatLng)
//                    val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            googleMap.addPolyline(lineoption)
        }
    }

    public fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }
}
