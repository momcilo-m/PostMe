package com.momcilo.postme.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
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
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import com.momcilo.postme.data.entities.NavItem
import com.momcilo.postme.data.entities.TempLoc
import com.momcilo.postme.data.states.AuthState
import com.momcilo.postme.ui.viewModels.DeliveryViewModel
import com.momcilo.postme.ui.viewModels.MapViewModel
import com.momcilo.postme.ui.viewModels.UserViewModel

@Composable
fun NavHostComposable(
    navController: NavHostController,
    start: Screen,
    padding: PaddingValues,
    vm: UserViewModel,
    locVm: MapViewModel,
    dVm: DeliveryViewModel
) {
    NavHost(
        navController,
        startDestination = start,
        modifier = Modifier.padding(padding)
    ) {
        composable<Screen.Login> { LoginScreen(vm, goToRegister = {navController.navigate(Screen.Register)}) }
        composable<Screen.Home> { HomeScreen(vm = dVm, map = locVm, nav = navController) }
        composable<Screen.Profile> { ProfileScreen(vm) }
        composable<Screen.Register> { RegisterScreen(userViewModel = vm) }
        composable<Screen.Loading> { LoadingScreen() }
        composable<Screen.Leaderboard> { LeaderboardScreen(vm) }
        composable<Screen.Map> {
            val args = it.toRoute<Screen.Map>()
            MapScreen(locVm,args.fromHome)
        }
    }
}

@Composable
fun NavigationBarComposable(
    paths:List<CustomNavItem>,
    currentRoute: String?,
    navController: NavController)
{
    NavigationBar {
        paths.forEach { item->
            NavigationBarItem(
                selected = currentRoute?.split(".")?.last() == item.name,
                onClick = {
                    navController.navigate(item.screen)
                },
                label = {Text(item.name)},
                icon = {

                    Icon(
                        imageVector = if(currentRoute == item.screen.javaClass.simpleName) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.name
                    )

                }
            )
        }
    }
}

@Composable
fun Main(vm: UserViewModel,locVm: MapViewModel,dVm: DeliveryViewModel)
{
    val navController = rememberNavController();
    val state = vm.authState.collectAsState()
    val startScreen = Screen.Loading;

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    val paths = listOf(
        CustomNavItem("Home", Screen.Home, Icons.Filled.Home, Icons.Outlined.Home),
        CustomNavItem("Map", Screen.Map(false), Icons.Filled.Place, Icons.Outlined.Place),
        CustomNavItem("Leaderboard", Screen.Leaderboard, Icons.Filled.Menu, Icons.Outlined.Menu),
        CustomNavItem("Profile", Screen.Profile, Icons.Filled.Person, Icons.Outlined.Person)
    )

    LaunchedEffect(state.value) {
        if(state.value is AuthState.Authenticated)
        {
            navController.navigate(Screen.Home){popUpTo(0){inclusive = true} }
            dVm.loadDelToFinish()
        }
        else if(state.value is AuthState.Unauthenticated)
        {
            navController.navigate(Screen.Login){popUpTo(0){inclusive = true} }
        }
        else if(state.value is AuthState.RegistrationSuccess) {
            navController.navigate(Screen.Login) { popUpTo(0) { inclusive = true } }
        }
    }

    Scaffold(
        bottomBar = {
            if(currentRoute?.split(".")?.last() != Screen.Login.javaClass.simpleName && currentRoute?.split(".")?.last() != Screen.Register.javaClass.simpleName)
            {
                NavigationBarComposable(paths,currentRoute,navController)
            }
        }
    ) { innerPadding->
        NavHostComposable(navController, startScreen,innerPadding,vm,locVm,dVm)
    }
}
