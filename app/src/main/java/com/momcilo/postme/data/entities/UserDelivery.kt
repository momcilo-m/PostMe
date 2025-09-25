package com.momcilo.postme.data.entities



data class UserDelivery(
    val user: String = "",
    val delivery: String = "",
    val location: TempLoc = TempLoc()
)
