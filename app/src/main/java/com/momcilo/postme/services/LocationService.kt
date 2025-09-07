package com.momcilo.postme.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.firestore.GeoPoint
import com.momcilo.postme.R
import com.momcilo.postme.activities.PostMeApplication
import com.momcilo.postme.data.repositories.DeliveryRepository
import com.momcilo.postme.data.repositories.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class LocationService : Service()
{
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationListener: LocationListener
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: UserRepository
    private lateinit var deliveryRepository: DeliveryRepository

    //Not Bind
    override fun onBind(p0: Intent?): IBinder? {
       return null;
    }

    override fun onCreate() {
        super.onCreate()

        repository = (application as PostMeApplication).userRepo
        deliveryRepository = (application as PostMeApplication).deliveryRepo

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)

        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            60_000L
        ).setMinUpdateIntervalMillis(30_000L)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "locationservicechannel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }


        val notification = NotificationCompat.Builder(this, "locationservicechannel")
            .setContentTitle("Tracking location…")
            .setContentText("Location: unknown")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)

        startForeground(1,notification.build())
        startTrack();
        return START_STICKY
    }

    private fun sendLocation(loc: Location)
    {
        serviceScope.launch {
            try {
                repository.sendLoaction(loc)
            }
            catch (e: Exception)
            {
                Log.d("LOCATION",e.toString())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startTrack() {
        //val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        locationListener = object : LocationListener
        {
            override fun onLocationChanged(loc: Location) {
                Log.d("LOCATION", "Lat: ${loc.latitude}, Lng: ${loc.longitude}")
                sendLocation(loc);
                deliveryRepository.updateQueryCenter(GeoPoint(loc.latitude,loc.longitude))
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationListener,
            Looper.getMainLooper()
        )
    }

    private fun stopTracking() {
        if (::locationListener.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationListener)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTracking()
    }

    private fun checkNearby(loc: Location)
    {

    }


}