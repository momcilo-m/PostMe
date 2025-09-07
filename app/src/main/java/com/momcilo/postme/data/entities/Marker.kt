package com.momcilo.postme.data.entities

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

data class Marker(
    //Kada korisnik kreira
    var user: String = "",
    val title: String = "",
    val address: Position = Position(),
    val description: String ="",
    val position: Position = Position(),

    var id:String="",
    val status : String = "Pending",
    val deliverer: String = "",

    var g:String = "",
    var l: GeoPoint? = null,
)

