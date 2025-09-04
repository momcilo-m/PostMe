package com.momcilo.postme.data.entities

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val name: String,
    val path: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val notification: Boolean = false,
    val countNotification: Int? = null
)