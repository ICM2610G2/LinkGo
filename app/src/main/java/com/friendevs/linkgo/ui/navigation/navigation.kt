package com.friendevs.linkgo.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.friendevs.linkgo.ui.feature.map.AddHotspotScreen
import com.friendevs.linkgo.ui.feature.chat.ChatDetailScreen
import com.friendevs.linkgo.ui.feature.chat.ChatScreen
import com.friendevs.linkgo.ui.feature.feed.FeedScreen
import com.friendevs.linkgo.ui.feature.map.HotspotsScreen
import com.friendevs.linkgo.ui.feature.auth.LoginScreen
import com.friendevs.linkgo.ui.feature.auth.LoginViewModel
import com.friendevs.linkgo.ui.feature.map.MapScreen
import com.friendevs.linkgo.ui.feature.meetup.MeetUpsScreen
import com.friendevs.linkgo.ui.feature.profile.ProfileScreen
import com.friendevs.linkgo.ui.feature.auth.RegisterScreen
import com.friendevs.linkgo.ui.feature.auth.RegisterViewModel

enum class Screens {
    Map,
    Feed,
    Chat,
    ChatDetail,
    Hotspots,
    Profile,
    MeetUp,
    login,
    register,

    AddHotspot
}

@Composable
@ExperimentalMaterial3Api
fun Navigation() {
    val navController = rememberNavController()

    val loginViewModel: LoginViewModel = viewModel()
    val registerViewModel: RegisterViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Screens.Map.name,
        Screens.Feed.name,
        Screens.Chat.name,
        Screens.Hotspots.name,
        Screens.Profile.name,
        Screens.AddHotspot.name
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BottomNavBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding -> 
        NavHost(
            navController = navController,
            startDestination = Screens.login.name,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(route = Screens.login.name) {
                LoginScreen(navController, loginViewModel)
            }
            composable(route = Screens.register.name) {
                RegisterScreen(navController, registerViewModel)
            }
            composable(route = Screens.Map.name) {
                MapScreen(navController)
            }
            composable(route = Screens.Feed.name) {
                FeedScreen(navController)
            }
            composable(route = Screens.Chat.name) {
                ChatScreen(navController)
            }
            composable(route = Screens.ChatDetail.name) {
                ChatDetailScreen(navController)
            }
            composable(route = Screens.Hotspots.name) {
                HotspotsScreen(navController)
            }
            composable(route = Screens.Profile.name) {
                ProfileScreen(navController)
            }
            composable(route = Screens.MeetUp.name) {
                MeetUpsScreen(navController)
            }
            composable(route = Screens.AddHotspot.name) {
                AddHotspotScreen(navController)
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String?) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val navItems = listOf(
            Screens.Map to Pair(Icons.Default.LocationOn, "MAP"),
            Screens.Feed to Pair(Icons.Default.Favorite, "FEED"),
            Screens.Chat to Pair(Icons.Default.Send, "CHAT"),
            Screens.Hotspots to Pair(Icons.Default.Home, "HOTSPOTS"),
            Screens.Profile to Pair(Icons.Default.Person, "PROFILE")
        )

        navItems.forEach { (screen, info) ->
            val (icon, label) = info
            NavigationBarItem(
                selected = currentRoute == screen.name,
                onClick = {
                    if (currentRoute != screen.name) {
                        navController.navigate(screen.name) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(icon, contentDescription = null) },
                label = { Text(label) }
            )
        }
    }
}
