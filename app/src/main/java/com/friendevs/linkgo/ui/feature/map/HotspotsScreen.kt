package com.friendevs.linkgo.ui.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.friendevs.linkgo.ui.navigation.Screens
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotspotsScreen(navController: NavController, mapViewModel: MapViewModel) {
    val state = mapViewModel.state
    var groupsExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            mapViewModel.loadHotSpots()
            mapViewModel.observeGroupsAndLocations(userId)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { topBarHostpots(navController) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 8.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = groupsExpanded,
                onExpandedChange = { if (state.myGroups.isNotEmpty()) groupsExpanded = !groupsExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = state.myGroups.firstOrNull { it.id == state.selectedGroupId }?.name
                        ?: "Sin grupos",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Grupo activo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupsExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = groupsExpanded,
                    onDismissRequest = { groupsExpanded = false }
                ) {
                    state.myGroups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.name.ifBlank { "Grupo sin nombre" }) },
                            onClick = {
                                mapViewModel.selectGroup(group.id)
                                groupsExpanded = false
                            }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(state.hotspots) { hotspot ->
                    HotspotCard(
                        hotspot = hotspot,
                        onClick = {
                            navController.navigate(
                                "${Screens.HotspotGallery.name}/${hotspot.id}/${
                                    hotspot.name.replace("/", "-")
                                }"
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun topBarHostpots(navController: NavController) {
    TopAppBar(
        title = {
            Text(
                text = "Hotspots",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = { navController.navigate(Screens.AddHotspot.name) },
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
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}