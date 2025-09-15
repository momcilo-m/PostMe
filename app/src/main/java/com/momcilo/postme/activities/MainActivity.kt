package com.momcilo.postme.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.momcilo.postme.services.LocationService
import com.momcilo.postme.ui.viewModels.UserViewModel
import com.momcilo.postme.ui.viewModels.UserViewModelFactory
import com.momcilo.postme.ui.screens.Main
import com.momcilo.postme.ui.theme.PostMeTheme
import com.momcilo.postme.ui.viewModels.DeliveryViewModel
import com.momcilo.postme.ui.viewModels.DeliveryViewModelFactory
import com.momcilo.postme.ui.viewModels.LocationViewModel
import com.momcilo.postme.ui.viewModels.MapViewModel
import com.momcilo.postme.ui.viewModels.LocationViewModelFactory
import com.momcilo.postme.ui.viewModels.MapViewModelFactory
import kotlin.jvm.java

class MainActivity : ComponentActivity() {

    private val userViewModel : UserViewModel by viewModels()
    {
        UserViewModelFactory((application as PostMeApplication).userRepo);
    }

    private val mapViewModel: MapViewModel by viewModels()
    {
        MapViewModelFactory(this,(application as PostMeApplication).deliveryRepo);
    };

    private val deliveryViewModel: DeliveryViewModel by viewModels()
    {
        DeliveryViewModelFactory(this,(application as PostMeApplication).deliveryRepo);
    };


    private val locationViewModel : LocationViewModel by viewModels()
    {
        LocationViewModelFactory(
            this,
            (application as PostMeApplication).userRepo,
            (application as PostMeApplication).deliveryRepo
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkLocationPermission();

        enableEdgeToEdge()
        setContent {
            PostMeTheme {
                Main(userViewModel,mapViewModel,locationViewModel,deliveryViewModel)
            }
        }
    }


    private fun checkLocationPermission() {
        val permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val notGranted = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                notGranted.toTypedArray(),
                10001
            )
        } else {
            startLocationLogic()
        }
    }

    private fun startLocationLogic() {
        locationViewModel.startLocationUpdates()
         val intent = Intent(this, LocationService::class.java)
         startForegroundService(intent)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 10001) {
            val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allGranted) {
                startLocationLogic()
            } else {
                Log.d("LOCATIONN","PERMISSIONS DENIED")
            }
        }
    }
}
