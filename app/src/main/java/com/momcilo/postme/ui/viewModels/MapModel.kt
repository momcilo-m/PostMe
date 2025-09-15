package com.momcilo.postme.ui.viewModels

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
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
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.rememberCameraPositionState
import com.momcilo.postme.R
import com.momcilo.postme.data.entities.Marker
import com.momcilo.postme.data.entities.Position
import com.momcilo.postme.data.repositories.DeliveryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MapViewModel(
    val app: ComponentActivity,
    private val repository: DeliveryRepository
): ViewModel()
{

    //(
    //    replay = 0,
    //    extraBufferCapacity = 1
    //)
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()


    init {
        viewModelScope.launch {
            val res = repository.loadPendingDeliveries();
            res.onSuccess {result->
                _markers.clear()
                _markers.addAll(result)
            }
            res.onFailure {fail->
                _toastEvent.emit(fail.localizedMessage ?: fail.toString());
            }

            repository.startGeoQuery(){ marker ->
                val exists = _markers.any { it.id == marker.id }
                if (!exists) {
                    _markers.add(marker)
                }
            };
        }
    }

    fun addMarker(name: String,address: Position, description: String,location: Position)
    {
        viewModelScope.launch {
            val res = repository.createDelivery(Marker("me",name, address,description,location))

            res.onSuccess {
                    marker -> _markers.add(Marker("me",name, address,description, location));
            }
        }
    }

    fun takeDelivery(id: String)
    {
        viewModelScope.launch {
            val res = repository.takeDelivery(id);

            res.onSuccess { res->

            }

            res.onFailure {e->
                _toastEvent.emit(e.localizedMessage ?: e.toString());
            }
        }
    }

    fun loadDeliveryWithFilter(user:String,radius: String, status: String)
    {
        viewModelScope.launch {
            val res = repository.loadFilteredDeliveries(user,radius,status);
            _markers.clear()
            _markers.addAll(res);
        }
    }

    fun showDeliveryLocation(position: Position, address: Position) {
        returnLocation = LatLng(position.latitude, position.longitude)
        deliveryLocation = LatLng(address.latitude, address.longitude)
    }

    fun animateCamera(scope:CoroutineScope,position: LatLng)
    {
        Log.d("MARKER","VRACANJE ${position.latitude}, ${position.longitude}")
        if(position.latitude != 0.0 && position.longitude !=0.0) {
            scope.launch {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(position.latitude, position.longitude),
                        15f
                    )
                )
            }
        }
    }

    //Lista markera u mapi
    private val _markers = mutableStateListOf<Marker>()
    val markers : List<Marker> = _markers;

    //Dodavanje markera
    private val _title = mutableStateOf<String>("")
    val title: MutableState<String> = _title

    private val _address = mutableStateOf(Position(0.0, 0.0))
    val address: MutableState<Position> = _address

    private val _description = mutableStateOf<String>("")
    val description: MutableState<String> = _description


    //Filter map
    private val _filterUser = mutableStateOf<String>("");
    val filterUser :MutableState<String> = _filterUser;

    private val _filterRadius = mutableStateOf<String>("");
    val filterRadius :MutableState<String> = _filterRadius;

    private val _filterStatus = mutableStateOf<String>("");
    val filterStatus :MutableState<String> = _filterStatus;

    private val _filterDistance = mutableStateOf<String>("");
    val filterDistance :MutableState<String> = _filterDistance;

    //Kamera na mapi
    var cameraSet by mutableStateOf(false)
    val cameraPositionState = CameraPositionState()
    var deliveryLocation by mutableStateOf(LatLng(0.0, 0.0))
    var returnLocation by mutableStateOf(LatLng(0.0, 0.0))
}


class MapViewModelFactory(private val app: ComponentActivity,private val repository: DeliveryRepository): ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(MapViewModel::class.java))
        {
            return MapViewModel(app,repository) as T;
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}