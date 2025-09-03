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
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.momcilo.postme.services.LocationService

class LocationViewModel(val app: ComponentActivity): ViewModel()
{

    private val _location  = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    //Provider za prikupljanje lokacije
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(app)

    @SuppressLint("MissingPermission")
    fun initLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    _location.value = location
                } else {
                    Log.d("LOCATION", "Lokacija nije dostupna")
                }
            }
    }

}


class LocationViewModelFactory(private val app: ComponentActivity): ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(LocationViewModel::class.java))
        {
            return LocationViewModel(app) as T;
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}