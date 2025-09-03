package com.momcilo.postme.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.momcilo.postme.data.NavItem
import com.momcilo.postme.data.states.AuthState
import com.momcilo.postme.ui.viewModels.LocationViewModel
import com.momcilo.postme.ui.viewModels.MapViewModel
import com.momcilo.postme.ui.viewModels.UserViewModel

@Composable
fun Main(vm: UserViewModel,locVm: MapViewModel,lVm: LocationViewModel)
{
    val navController = rememberNavController();
    val state = vm.authState.collectAsState()
    val startScreen = "loading";

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val paths = listOf(
        NavItem("Home","home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem("Map","map", Icons.Filled.Place, Icons.Outlined.Place),
        NavItem("Profile","profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    LaunchedEffect(state.value) {
        if(state.value is AuthState.Authenticated)
        {
            navController.navigate("home"){popUpTo(0){inclusive = true} }
        }
        else if(state.value is AuthState.Unauthenticated)
        {
            navController.navigate("login"){popUpTo(0){inclusive = true} }
        }
        else if(state.value is AuthState.RegistrationSuccess) {
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        }
    }

    Scaffold(
        bottomBar = {
            if(currentRoute != "login" && currentRoute != "register")
            {
                NavigationBarComposable(paths,currentRoute,navController)
            }

        }
    ) { innerPadding->
        NavHostComposable(navController,"loading",innerPadding,vm,locVm,lVm)
    }
}

@Composable
fun NavigationBarComposable(
    paths:List<NavItem>,
    currentRoute: String?,
    navController: NavController)
{
    NavigationBar {
        paths.forEach { item->
            NavigationBarItem(
                selected = currentRoute == item.path,
                onClick = {
                    //selectedScreen = index
                    navController.navigate(item.path)
                },
                label = {Text(item.name)},
                icon = {
                    BadgedBox(
                        badge = {
                            if(item.countNotification != null)
                            {
                                Badge(){Text(item.countNotification.toString())}
                            }
                            else if(item.notification)
                            {
                                Badge()
                            }
                        }
                    )
                    {
                        Icon(
                            imageVector = if(currentRoute == item.path) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.name
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun NavHostComposable(
    navController: NavHostController,
    start: String,
    padding: PaddingValues,
    vm: UserViewModel,
    locVm: MapViewModel,
    lVm: LocationViewModel
) {
    NavHost(
        navController,
        startDestination = start,
        modifier = Modifier.padding(padding)
    ) {
        composable("login") { LoginScreen(vm) }
        composable("home") { HomeScreen() }
        composable("register") { RegisterScreen(userViewModel = vm) }
        composable("loading") { LoadingScreen() }
        composable("map") { MapScreen(locVm,lVm) }
    }
}
