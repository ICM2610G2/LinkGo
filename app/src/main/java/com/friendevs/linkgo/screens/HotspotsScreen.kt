package com.friendevs.linkgo.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.friendevs.linkgo.components.HotspotCard
import com.friendevs.linkgo.model.Hotspot
import com.friendevs.linkgo.navigation.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotspotsScreen(navController: NavController) {

    Scaffold(
        containerColor = Color(0xFF140F23),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Hotspots",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF140F23)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF140F23)
            ) {

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screens.Map.name) },
                    icon = { Icon(Icons.Default.LocationOn, null, tint = Color.White) },
                    label = { Text("MAP", color = Color.White) }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screens.Feed.name) },
                    icon = { Icon(Icons.Default.Favorite, null, tint = Color.White) },
                    label = { Text("FEED", color = Color.White) }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screens.Chat.name) },
                    icon = { Icon(Icons.Default.Send, null, tint = Color.White) },
                    label = { Text("CHAT", color = Color.White) }
                )

                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Home, null, tint = Color.White) },
                    label = { Text("HOTSPOTS", color = Color.White) }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screens.Profile.name) },
                    icon = { Icon(Icons.Default.Person, null, tint = Color.White) },
                    label = { Text("PROFILE", color = Color.White) }
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(Hotspot.hotspotList) { hotspot ->
                HotspotCard(hotspot = hotspot)
            }
        }
    }
}