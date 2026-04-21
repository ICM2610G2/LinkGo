package com.friendevs.linkgo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.friendevs.linkgo.components.HotspotCard
import com.friendevs.linkgo.model.Hotspot
import com.friendevs.linkgo.navigation.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotspotsScreen(navController: NavController) {

    Scaffold(
        containerColor = Color(0xFF140F23),
        topBar = { topBarHostpots() }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun topBarHostpots() {
    TopAppBar(
        title = {
            Text(
                text = "Hotspots",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
            )
        },

        //Icono de la barra Menu
        navigationIcon = {

            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Menu",
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        //Icono de busqueda
        actions = {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}