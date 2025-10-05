package com.momcilo.postme.ui.screens

import androidx.compose.ui.graphics.vector.ImageVector

data class CustomNavItem(
    val name: String,
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)