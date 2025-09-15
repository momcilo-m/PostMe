package com.momcilo.postme.ui.viewModels

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.GeoPoint
import com.momcilo.postme.data.repositories.DeliveryRepository
import com.momcilo.postme.data.repositories.UserRepository

class LocationViewModel(
    private val app: ComponentActivity,
    private val userRepository: UserRepository,
    private val deliveryRepository: DeliveryRepository
): ViewModel()
{

    private val _location  = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(app)

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        Log.d("LOCATIONN","REQUEST")
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000L
        ).setMinUpdateIntervalMillis(5_000L)
            .build()


        val locationListener = object : LocationListener
        {
            override fun onLocationChanged(loc: Location) {
                val current = _location.value
                if (current == null || current.latitude != loc.latitude || current.longitude != loc.longitude) {
                    _location.value = loc
                    deliveryRepository.updateQueryCenter(GeoPoint(loc.latitude,loc.longitude))
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            request,
            locationListener,
            Looper.getMainLooper()
        )
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(object : LocationCallback(){})
    }
}


class LocationViewModelFactory(private val app: ComponentActivity,
                               private val userRepository: UserRepository,
                               private val deliveryRepository: DeliveryRepository): ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(LocationViewModel::class.java))
        {
            return LocationViewModel(app,userRepository,deliveryRepository) as T;
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}