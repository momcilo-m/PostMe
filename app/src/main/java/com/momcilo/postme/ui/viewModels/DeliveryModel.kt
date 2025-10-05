package com.momcilo.postme.ui.viewModels

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.momcilo.postme.data.cache.MarkerCache
import com.momcilo.postme.data.entities.Marker
import com.momcilo.postme.data.repositories.DeliveryRepository
import com.momcilo.postme.data.repositories.UserRepository
import kotlinx.coroutines.launch

class DeliveryViewModel(
    //    private val app: ComponentActivity,
    //    private val userRepository: UserRepository,
    private val deliveryRepository: DeliveryRepository
): ViewModel()
{

    private val _markers = mutableStateListOf<Marker>()
    val markers : List<Marker> = _markers;


    init {
        viewModelScope.launch {

            deliveryRepository.loadDeliveryToFinish()
                .onSuccess { markers->
                    _markers.clear()
                    _markers.addAll(markers);
                    MarkerCache.clear()
                    MarkerCache.addAll(markers);
                }
                .onFailure {err->
                    Log.d("DELIVERY",err.toString())
                }

        }
    }

    fun finishDelivery(id: String)
    {
        viewModelScope.launch {
            deliveryRepository.finishDelivery(id)
                .onSuccess {
                    Log.d("DELIVERY","FINISH DEL")
                    _markers.removeIf { it.id == id }
                    MarkerCache.send.removeIf { it.id == id }
                    MarkerCache.temp.removeIf { it.id == id  }
                }
                .onFailure {e->
                    Log.d("DELIVERY","FAIL FINISH $e")
                }
        }
    }
}


class DeliveryViewModelFactory(private val repository: DeliveryRepository): ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        @Suppress("UNCHECKED_CAST")
        if(modelClass.isAssignableFrom(DeliveryViewModel::class.java))
        {
            return DeliveryViewModel(repository) as T;
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}