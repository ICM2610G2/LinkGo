package com.friendevs.linkgo.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.friendevs.linkgo.ui.feature.map.AddHotspotScreen
import com.friendevs.linkgo.ui.feature.map.HotspotGalleryScreen
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
import com.friendevs.linkgo.ui.feature.map.MapViewModel
import kotlinx.coroutines.delay

enum class Screens {
    Map, Feed, Chat, ChatDetail, Hotspots, Profile, MeetUp, login, register, AddHotspot, HotspotGallery
}

@Composable
@ExperimentalMaterial3Api
fun Navigation(
    sensorViewModel: com.friendevs.linkgo.model.SensorViewModel,
    deepLinkGroupId: String? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val sheetState = rememberModalBottomSheetState()
    var showSafetySheet by remember { mutableStateOf(false) }

    val loginViewModel: LoginViewModel = viewModel()
    val registerViewModel: RegisterViewModel = viewModel()
    val mapViewModel : MapViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(
        Screens.Map.name, Screens.Feed.name, Screens.Chat.name,
        Screens.Hotspots.name, Screens.Profile.name, Screens.AddHotspot.name
    )

    // Deep-link de notificacion: navega al chat del grupo y consume el evento.
    LaunchedEffect(deepLinkGroupId) {
        val gid = deepLinkGroupId
        if (!gid.isNullOrEmpty()) {
            navController.navigate("${Screens.ChatDetail.name}/$gid") {
                launchSingleTop = true
            }
            onDeepLinkConsumed()
        }
    }

    LaunchedEffect(sensorViewModel.shakeDetected) {
        if (sensorViewModel.shakeDetected) {
            showSafetySheet = true
            delay(100)
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
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Screens.login.name,
                // El mapa se dibuja full-bleed (bajo el bottom bar); el resto reserva su alto.
                modifier = Modifier.padding(
                    bottom = if (currentRoute == Screens.Map.name) 0.dp
                    else innerPadding.calculateBottomPadding()
                )
            ) {
                composable(route = Screens.login.name) { LoginScreen(navController, loginViewModel) }
                composable(route = Screens.register.name) { RegisterScreen(navController, registerViewModel) }
                composable(route = Screens.Map.name) {
                    MapScreen(navController, viewModel = mapViewModel, sensorViewModel = sensorViewModel)
                }
                composable(route = Screens.Feed.name) { FeedScreen(navController, sensorViewModel = sensorViewModel) }
                composable(route = Screens.Chat.name) { ChatScreen(navController) }
                composable(
                    route = "${Screens.ChatDetail.name}/{groupId}",
                    arguments = listOf(navArgument("groupId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                    ChatDetailScreen(navController, groupId = groupId, sensorViewModel = sensorViewModel)
                }
                composable(route = Screens.Hotspots.name) { HotspotsScreen(navController, mapViewModel = mapViewModel) }
                composable(route = Screens.Profile.name) { ProfileScreen(navController) }
                composable(route = Screens.MeetUp.name) { MeetUpsScreen(navController, mapViewModel = mapViewModel) }
                composable(route = Screens.AddHotspot.name) {
                    AddHotspotScreen(navController, mapViewModel = mapViewModel)
                }
                composable(
                    route = "${Screens.HotspotGallery.name}/{hotspotId}/{hotspotName}",
                    arguments = listOf(
                        navArgument("hotspotId") { type = NavType.StringType },
                        navArgument("hotspotName") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val hotspotId = backStackEntry.arguments?.getString("hotspotId") ?: ""
                    val hotspotName = backStackEntry.arguments?.getString("hotspotName") ?: ""
                    HotspotGalleryScreen(
                        navController = navController,
                        hotspotId = hotspotId,
                        hotspotName = hotspotName,
                        mapViewModel = mapViewModel
                    )
                }
            }

            if (showSafetySheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSafetySheet = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Maniobra brusca detectada",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "¿Deseas llamar a emergencias?",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:123")
                                }
                                context.startActivity(intent)
                                showSafetySheet = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Llamar al 123", color = androidx.compose.ui.graphics.Color.White)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { showSafetySheet = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar")
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
            Screens.Feed to Pair(Icons.Default.List, "FEED"),
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
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
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