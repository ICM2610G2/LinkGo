package com.friendevs.linkgo.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.friendevs.linkgo.screens.AddHotspotScreen
import com.friendevs.linkgo.screens.ChatDetailScreen
import com.friendevs.linkgo.screens.ChatScreen
import com.friendevs.linkgo.screens.FeedScreen
import com.friendevs.linkgo.screens.HotspotsScreen
import com.friendevs.linkgo.screens.LoginScreen
import com.friendevs.linkgo.screens.LoginViewModel
import com.friendevs.linkgo.screens.MapScreen
import com.friendevs.linkgo.screens.MeetUpsScreen
import com.friendevs.linkgo.screens.ProfileScreen
import com.friendevs.linkgo.screens.RegisterScreen
import com.friendevs.linkgo.screens.RegisterViewModel

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
fun Navigation(sensorViewModel: com.friendevs.linkgo.model.SensorViewModel) {
    val navController = rememberNavController()

    val sheetState = rememberModalBottomSheetState()
    var showSafetySheet by remember { mutableStateOf(false) }

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

    LaunchedEffect(sensorViewModel.shakeDetected) {
        if (sensorViewModel.shakeDetected) {
            showSafetySheet = true
            sensorViewModel.resetShake()
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                BottomNavBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())
        ){
            NavHost(
                navController = navController,
                startDestination = Screens.login.name,
            ) {
                composable(route = Screens.login.name) {
                    LoginScreen(navController, loginViewModel)
                }
                composable(route = Screens.register.name) {
                    RegisterScreen(navController, registerViewModel)
                }
                composable(route = Screens.Map.name) {
                    MapScreen(navController, sensorViewModel = sensorViewModel)
                }
                composable(route = Screens.Feed.name) {
                    FeedScreen(navController, sensorViewModel = sensorViewModel)
                }
                composable(route = Screens.Chat.name) {
                    ChatScreen(navController)
                }
                composable(route = Screens.ChatDetail.name) {
                    ChatDetailScreen(navController, sensorViewModel = sensorViewModel)
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
            if (showSafetySheet) {
                androidx.compose.material3.ModalBottomSheet(
                    onDismissRequest = { showSafetySheet = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Maniobra brusca detectada",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "¿Deseas llamar a emergencias?",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                        androidx.compose.material3.Button(
                            onClick = {
                                showSafetySheet = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Llamar", color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
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
