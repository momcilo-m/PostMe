package com.momcilo.postme.data.entities

import com.google.android.gms.maps.model.LatLng

data class Position(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    fun toLatLng() = LatLng(latitude, longitude)
}
