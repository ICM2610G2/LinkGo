package com.friendevs.linkgo.screens

import android.R.color.white
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.friendevs.linkgo.R
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
                    onClick = { navController.navigate(Screens.Map.name) },
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {

                Image(
                    painter = painterResource(R.drawable.mapa),
                    contentDescription = "mapa",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Button(
                    onClick = { navController.navigate(Screens.Map.name) },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 35.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Meet up"
                        )
                        Text(
                            text = "Meet Up"
                        )
                    }
                }


            }


        }

    }

}

@Composable
@Preview
fun mostrarMap() {
    MapScreen(navController = NavController(LocalContext.current))
}