package com.momcilo.postme.data.entities


data class TempLoc(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)


data class User(
    val username: String = "",
    val email: String = "",
    val phone: String = "",
    val name: String = "",
    val lastName: String = "",
    val points: Int = 0,
    var photo: String = "",
    val location: TempLoc? = null
)
