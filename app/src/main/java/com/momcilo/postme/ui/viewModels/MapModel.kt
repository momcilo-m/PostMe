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
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.momcilo.postme.data.entities.Marker
import com.momcilo.postme.data.entities.Position
import com.momcilo.postme.data.repositories.DeliveryRepository
import kotlinx.coroutines.launch

class MapViewModel(private val repository: DeliveryRepository): ViewModel()
{

    private val _markers =mutableStateListOf<Marker>()
    val markers : List<Marker> = _markers;

    fun addMarker(name: String,address: Position, description: String,location: Position)
    {
        viewModelScope.launch {
            val res = repository.createDelivery(Marker("me",name, address,description, location))

            res.onSuccess {
                marker -> _markers.add(Marker("me",name, address,description, location));
            }
        }
    }

    init {
        viewModelScope.launch {
            val res = repository.loadDeliveries();
            res.onSuccess {result->
                _markers.clear()
                _markers.addAll(result)
            }
            res.onFailure {fail->
                Log.d("MARKERI",fail.localizedMessage ?: fail.toString());
            }
        }
    }

    fun takeDelivery(id: String)
    {
        viewModelScope.launch {
            val res = repository.takeDelivery(id);

            res.onSuccess { res->

            }

            res.onFailure {
                e-> Log.d("MARKERI",e.localizedMessage ?: e.toString())
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
}


class MapViewModelFactory(private val repository: DeliveryRepository): ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(MapViewModel::class.java))
        {
            return MapViewModel(repository) as T;
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}