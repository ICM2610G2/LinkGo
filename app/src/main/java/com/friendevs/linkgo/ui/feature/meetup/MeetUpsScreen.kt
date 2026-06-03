package com.friendevs.linkgo.ui.feature.meetup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.friendevs.linkgo.ui.feature.meetup.MeetUpCard
import com.friendevs.linkgo.domain.model.MeetUpContact
import com.friendevs.linkgo.ui.feature.map.MapViewModel
import com.friendevs.linkgo.ui.navigation.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetUpsScreen(navController: NavController, mapViewModel: MapViewModel) {
    val state = mapViewModel.state
    val selectedGroup = state.myGroups.firstOrNull { it.id == state.selectedGroupId }

    val contacts = if (state.meetupRoutes.isNotEmpty()) {
        state.meetupRoutes.map { route ->
            MeetUpContact(
                fullName = route.name,
                distance = route.distanceText,
                location = "ETA ${route.durationText}"
            )
        }
    } else {
        mapViewModel.selectedGroupMembersForMeetup().map { member ->
            MeetUpContact(
                fullName = member.name,
                distance = "Ubicacion compartida",
                location = "${"%.4f".format(member.lat)}, ${"%.4f".format(member.lng)}"
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Meet up", color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            selectedGroup?.let { "Grupo: ${it.name}" } ?: "Selecciona/crea un grupo en Chat",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Button(
                    onClick = {
                        mapViewModel.startMeetupToRandomHotspot()
                        navController.navigate(Screens.Map.name)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Calcular rutas del grupo", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (contacts.isEmpty()) {
                item {
                    Text(
                        text = "No hay miembros con ubicacion activa para este grupo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(contacts) { contact ->
                    MeetUpCard(contact = contact)
                }
            }

        }
    }
}