package com.momcilo.postme.ui.screens

import android.R
import com.momcilo.postme.data.entities.TempLoc
import kotlinx.serialization.Serializable
@Serializable
sealed class Screen(val route: String) {

    @Serializable
    object Home : Screen("home")
    @Serializable
    object Login : Screen("login")
    @Serializable
    object Register : Screen("register")
    @Serializable
    object Leaderboard : Screen("leaderboard")
    @Serializable
    object Loading : Screen("loading")
    @Serializable
    object Profile : Screen("profile")

    @Serializable
    data class Map(val fromHome: Boolean) : Screen("map")
}

