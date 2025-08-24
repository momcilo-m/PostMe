package com.momcilo.postme.data.entities

import com.google.android.gms.maps.model.LatLng

data class Marker(
    //Kada korisnik kreira
    var id: String="",
    var user: String = "",
    val title: String = "",
    val address: String = "",
    val description: String ="",
    val position:LatLng = LatLng(15.0,14.0),

    val status : String = "Pending",
    val delivered: String = "",
)
