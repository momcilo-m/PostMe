package com.momcilo.postme.data.entities

import com.google.android.gms.maps.model.LatLng

data class Marker(
    //Kada korisnik kreira
    var user: String = "",
    val title: String = "",
    val address: String = "",
    val description: String ="",
    val position: Position = Position(),

    var id:String="",
    val status : String = "Pending",
    val deliverer: String = "",
)

