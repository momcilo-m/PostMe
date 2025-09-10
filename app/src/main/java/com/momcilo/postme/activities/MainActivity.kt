package com.momcilo.postme.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.momcilo.postme.services.LocationService
import com.momcilo.postme.ui.viewModels.UserViewModel
import com.momcilo.postme.ui.viewModels.UserViewModelFactory
import com.momcilo.postme.ui.screens.Main
import com.momcilo.postme.ui.theme.PostMeTheme
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

    private val locationViewModel : LocationViewModel by viewModels()
    {
        LocationViewModelFactory(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkLocationPermission();

        enableEdgeToEdge()
        setContent {
            PostMeTheme {
                Main(userViewModel,mapViewModel,locationViewModel)
            }
        }
    }


    private fun checkLocationPermission() {
        val permissions = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Od Android 12+ dodaj FOREGROUND_SERVICE_LOCATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }

        // Provera koje permisije nisu odobrene
        val notGranted = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            // Traži sve neodobrene permisije
            ActivityCompat.requestPermissions(
                this,
                notGranted.toTypedArray(),
                10001
            )
        } else {
            // Sve permisije su odobrene → startuj servis i inicijalizuj ViewModel
            locationViewModel.initLocation()

            val intent = Intent(this, LocationService::class.java)
            startForegroundService(intent)
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)} passing\n      in a {@link RequestMultiplePermissions} object for the {@link ActivityResultContract} and\n      handling the result in the {@link ActivityResultCallback#onActivityResult(Object) callback}.")
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
                // Sve permisije odobrene → startuj servis i inicijalizuj ViewModel
                locationViewModel.initLocation()
                val intent = Intent(this, LocationService::class.java)
                startForegroundService(intent)
            } else {
                //Toast.makeText(this, "Location permissions are required", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

