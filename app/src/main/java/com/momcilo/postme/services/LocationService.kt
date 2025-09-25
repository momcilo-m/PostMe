package com.momcilo.postme.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


class LocationService : Service()
{

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private lateinit var repository: UserRepository
    private lateinit var deliveryRepository: DeliveryRepository
    private lateinit var notificationManager: NotificationManager;

    //Not Bind
    override fun onBind(p0: Intent?): IBinder? {
       return null;
    }

    override fun onCreate() {
        super.onCreate()
        repository = (application as PostMeApplication).userRepo
        deliveryRepository = (application as PostMeApplication).deliveryRepo
        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sendLocationRoutine();
        startForeground(1,createNotification(this, "Location are tracking","Location Tracking", "locationservicechannel"))

        deliveryRepository.startGeoQuery {
            sendNotification(this,"New delivery is near you","New Delivery","geo-document")
        }

        return START_STICKY
    }

    private fun sendLocationRoutine()
    {
        serviceScope.launch {
            while (isActive) {

                Log.d("SERVICELOC", "Salje se lokacija na srv")
                repository.sendLocation(deliveryRepository.userLocation)
                deliveryRepository.sendLocationDelivery();

                delay(60_000)
            }
        }
    }

    private fun createNotification(context: Context, message: String,title: String, channel:String): Notification {

        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
    }

    private fun sendNotification(context: Context, message: String,title: String, channel:String) {
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, createNotification(context, message,title, channel))
    }
}