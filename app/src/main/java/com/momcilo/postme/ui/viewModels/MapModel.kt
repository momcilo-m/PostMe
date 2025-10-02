package com.momcilo.postme.ui.viewModels

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
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
import com.momcilo.postme.activities.PostMeApplication
import com.momcilo.postme.data.cache.MarkerCache
import com.momcilo.postme.data.entities.Marker
import com.momcilo.postme.data.entities.Position
import com.momcilo.postme.data.repositories.DeliveryRepository
import com.momcilo.postme.data.repositories.LocationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MapViewModel(
    val app: PostMeApplication,
    private val repository: DeliveryRepository,
    private val locRepo: LocationRepository
): ViewModel()
{

    var notificationManager = app.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    //(
    //    replay = 0,
    //    extraBufferCapacity = 1
    //)
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent = _toastEvent.asSharedFlow()


    init {
        viewModelScope.launch {

            repository.initQuery()

            val res = repository.loadPendingDeliveries();
            res.onSuccess {result->
                Log.d("DELIVERY","STIGLE PORUDZBINE NA MAPI")
                _markers.clear()
                _markers.addAll(result)
            }
            res.onFailure {fail->
                _toastEvent.emit(fail.localizedMessage ?: fail.toString());
            }

            repository.startGeoQuery(){ marker ->
                if(marker.status == "pending")
                {
                    val exists = _markers.any { it.id == marker.id }//&& it.status != "pending"
                    if (!exists) {
                        _markers.add(marker)
                    }
                    sendNotification(app,"New delivery is near you","Slow down boy","geo-document")
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
                val marker = _markers.find { it.id == id }

                if(marker != null)
                    MarkerCache.add(marker);

                _markers.remove(marker)
                _toastEvent.emit("You are successfully take a delivery");
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
        showMarker = true;
    }

    private fun createNotification(context: Context, message: String,title: String, channel:String): Notification {

        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.baseline_local_shipping_24)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            //.setOngoing(true)
            .build()
    }

    private fun sendNotification(context: Context, message: String,title: String, channel:String) {
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, createNotification(context, message,title, channel))
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
    var showMarker by mutableStateOf(false);
    var deliveryLocation by mutableStateOf(LatLng(0.0, 0.0))
    var returnLocation by mutableStateOf(LatLng(0.0, 0.0))

    //Lokcaija korisnika
    val userLocation: LiveData<Location> = locRepo.location
}


class MapViewModelFactory(private val app: PostMeApplication,private val repository: DeliveryRepository,private val locRepo: LocationRepository): ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(MapViewModel::class.java))
        {
            return MapViewModel(app,repository,locRepo) as T;
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}