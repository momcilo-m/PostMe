package com.momcilo.postme.activities

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
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
import com.momcilo.postme.ui.viewModels.UserViewModel
import com.momcilo.postme.ui.viewModels.UserViewModelFactory
import com.momcilo.postme.ui.screens.Main
import com.momcilo.postme.ui.theme.PostMeTheme
import com.momcilo.postme.ui.viewModels.MapViewModel
import com.momcilo.postme.ui.viewModels.LocationViewModelFactory

class MainActivity : ComponentActivity() {

    private val userViewModel : UserViewModel by viewModels()
    {
        UserViewModelFactory((application as PostMeApplication).userRepo);
    }

    private val mapViewModel: MapViewModel by viewModels()
    {
        LocationViewModelFactory(this);
    };

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkLocationPermission();

        enableEdgeToEdge()
        setContent {
            PostMeTheme {
                Main(userViewModel,mapViewModel)
            }
        }
    }

    private fun checkLocationPermission()
    {
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                10001
            )
        }
        else
        {
            mapViewModel.initLocation();
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)

        if (requestCode == 10001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mapViewModel.initLocation()
        }
    }
}

