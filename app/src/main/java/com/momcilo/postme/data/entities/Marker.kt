package com.momcilo.postme.data.entities

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Marker(

    var user: String = "",
    val title: String = "",
    val address: Position = Position(),
    val description: String ="",
    val position: Position = Position(),
    val createdAt: Timestamp = Timestamp.now(),

    var id:String="",
    val status : String = "pending",
    val deliverer: String = "",


    var g:String = "",
    var l: GeoPoint? = null,
)

