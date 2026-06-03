package com.friendevs.linkgo.ui.feature.meetup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.friendevs.linkgo.domain.model.MeetUpContact
import com.friendevs.linkgo.ui.feature.map.MapViewModel
import com.friendevs.linkgo.ui.navigation.Screens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetUpsScreen(navController: NavController, mapViewModel: MapViewModel) {
    val state = mapViewModel.state
    val selectedGroup = state.myGroups.firstOrNull { it.id == state.selectedGroupId }

    val allMemberUids = state.groupMemberLocations.map { it.uid }.toSet()
    val selectedUids = state.selectedMemberUidsForMeetup.ifEmpty { allMemberUids }

    val contacts = if (state.meetupRoutes.isNotEmpty()) {
        state.meetupRoutes.map { route ->
            MeetUpContact(
                uid = route.uid,
                fullName = route.name,
                distance = route.distanceText,
                location = "ETA ${route.durationText}"
            )
        }
    } else {
        state.groupMemberLocations.map { member ->
            MeetUpContact(
                uid = member.uid,
                fullName = member.name,
                distance = "Ubicacion compartida",
                location = "${"%.4f".format(member.lat)}, ${"%.4f".format(member.lng)}"
            )
        }
    }

    val isMeetupActive = state.meetupRoutes.isNotEmpty()
    val noMemberSelected = selectedUids.isEmpty()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isMeetupActive) {
                    OutlinedButton(
                        onClick = {
                            mapViewModel.cancelMeetup()
                            navController.navigate(Screens.Map.name)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cancelar MeetUp")
                    }
                }
                Button(
                    onClick = {
                        mapViewModel.startMeetupToNearestHotspot()
                        navController.navigate(Screens.Map.name)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !noMemberSelected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        if (isMeetupActive) "Recalcular rutas" else "Iniciar MeetUp",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                item {
                    Text(
                        text = "Selecciona quiénes participan:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(contacts, key = { it.uid }) { contact ->
                    MeetUpCard(
                        contact = contact,
                        selected = contact.uid in selectedUids,
                        onToggle = { mapViewModel.toggleMemberForMeetup(contact.uid) }
                    )
                }
            }
        }
    }
}
