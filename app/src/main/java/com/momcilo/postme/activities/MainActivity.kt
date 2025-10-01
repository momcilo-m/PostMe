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
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.momcilo.postme.services.LocationService
import com.momcilo.postme.ui.viewModels.UserViewModel
import com.momcilo.postme.ui.viewModels.UserViewModelFactory
import com.momcilo.postme.ui.screens.Main
import com.momcilo.postme.ui.theme.PostMeTheme
import com.momcilo.postme.ui.viewModels.DeliveryViewModel
import com.momcilo.postme.ui.viewModels.DeliveryViewModelFactory
// com.momcilo.postme.ui.viewModels.LocationViewModel
import com.momcilo.postme.ui.viewModels.MapViewModel
//import com.momcilo.postme.ui.viewModels.LocationViewModelFactory
import com.momcilo.postme.ui.viewModels.MapViewModelFactory
import kotlin.jvm.java

class MainActivity : ComponentActivity() {

    //val postMe = (application as PostMeApplication);

    private val userViewModel : UserViewModel by viewModels()
    {
        UserViewModelFactory((application as PostMeApplication),(application as PostMeApplication).userRepo);
    }

    private val mapViewModel: MapViewModel by viewModels()
    {
        MapViewModelFactory((application as PostMeApplication),(application as PostMeApplication).deliveryRepo,(application as PostMeApplication).locationRepo);
    };

    private val deliveryViewModel: DeliveryViewModel by viewModels()
    {
        DeliveryViewModelFactory((application as PostMeApplication).deliveryRepo);
    };

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkLocationPermission();

        enableEdgeToEdge()
        setContent {
            PostMeTheme {
                Main(userViewModel,mapViewModel,deliveryViewModel)
            }
        }
    }


    //Zahteva zbog POST_NOTIFICATION
    private fun checkLocationPermission() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }


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
         val intent = Intent(this, LocationService::class.java)
         startForegroundService(intent)
    }

    @Deprecated("This method has been deprecated")
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
