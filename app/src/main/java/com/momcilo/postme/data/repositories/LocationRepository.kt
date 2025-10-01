package com.momcilo.postme.data.repositories

import android.annotation.SuppressLint
import android.content.Context
//import android.health.connect.datatypes.ExerciseRoute.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.GeoPoint
import android.location.Location

class LocationRepository(
    private val app: Context,
) {

    private val _location  = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.app)

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        //Log.d("LOCATION","REQUEST")

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L
        ).setMinUpdateIntervalMillis(5_000L)
            .build()


        val locationListener = object : LocationListener
        {
            @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            override fun onLocationChanged(loc: Location) {
                val current = _location.value
                if (current == null || current.latitude != loc.latitude || current.longitude != loc.longitude) {
                    _location.value = loc
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationListener,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(object : LocationCallback(){})
    }

}