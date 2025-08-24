package com.momcilo.postme.activities

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.momcilo.postme.data.repositories.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.momcilo.postme.data.repositories.DeliveryRepository

class PostMeApplication : Application() {
    lateinit var userRepo: UserRepository
        private set

    lateinit var deliveryRepo: DeliveryRepository
        private set

    override fun onCreate() {
        super.onCreate()

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        val auth = FirebaseAuth.getInstance()
        val userDb = FirebaseDatabase.getInstance("https://postme-c46da-default-rtdb.europe-west1.firebasedatabase.app/").reference

        val deliveryDb : FirebaseFirestore by lazy { Firebase.firestore }

        userRepo = UserRepository(auth, userDb)
        deliveryRepo = DeliveryRepository(auth,deliveryDb)
    }
}
