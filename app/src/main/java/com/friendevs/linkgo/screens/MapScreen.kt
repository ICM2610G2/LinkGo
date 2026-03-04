package com.friendevs.linkgo.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.friendevs.linkgo.navigation.Screens

@Composable
fun MapScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = {navController.navigate(Screens.Map.name) },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    label = { Text("MAP") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screens.Feed.name) },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    label = { Text("FEED") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screens.Chat.name) },
                    icon = { Icon(Icons.Default.Send, contentDescription = null) },
                    label = { Text("CHAT") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screens.Hotspots.name) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("HOTSPOTS") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screens.Profile.name) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("PROFILE") }
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Map Screen")
        }
    }
}