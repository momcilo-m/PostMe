package com.momcilo.postme.ui.viewModels

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.momcilo.postme.data.entities.Marker

class MapViewModel(app: ComponentActivity): ViewModel()
{
    private val _location  = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private val _markers =mutableStateListOf<Marker>()
    val markers : List<Marker> = _markers;

    fun addMarker(name: String,address: String, description: String,location: LatLng)
    {
        _markers.add(Marker("me",name, address,description,location))
        _title.value = ""
        _address.value=""
        _description.value=""
    }

    //Dodavanje markera
    private val _title = mutableStateOf<String>("")
    val title: MutableState<String> = _title

    private val _address = mutableStateOf<String>("")
    val address: MutableState<String> = _address

    private val _description = mutableStateOf<String>("")
    val description: MutableState<String> = _description

    //Funkcije za rad sa lokacijom i dozvolama

    //Provider za prikupljanje lokacije
    private var fusedLocationClient = LocationServices.getFusedLocationProviderClient(app)

    private var locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5000L
    ).setMinUpdateIntervalMillis(2000L)
    .build()

    private lateinit var locationListener: LocationListener

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

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startActiveLocation()
    {
        locationListener = object : LocationListener
        {
            override fun onLocationChanged(loc: Location) {
                _location.value = loc;
                Log.d("LOCATION", "Lat: ${loc.latitude}, Lng: ${loc.longitude}")
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationListener,
            Looper.getMainLooper()
        )
    }

    fun stopActiveLocation()
    {
        locationListener.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
}


class LocationViewModelFactory(private val app: ComponentActivity): ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(MapViewModel::class.java))
        {
            return MapViewModel(app) as T;
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}