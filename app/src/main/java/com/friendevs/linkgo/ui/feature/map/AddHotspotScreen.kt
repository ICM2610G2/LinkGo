package com.friendevs.linkgo.ui.feature.map

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.libraries.places.api.Places
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHotspotScreen(
    navController: NavController,
    mapViewModel: MapViewModel,
    viewModel: AddHotspotViewModel = viewModel()
) {
    val context = LocalContext.current
    val placesClient = remember { Places.createClient(context) }
    val cameraPositionState = rememberCameraPositionState()
    val state = viewModel.state

    LaunchedEffect(state.hotspotSaved) {
        if (state.hotspotSaved) {
            Toast.makeText(context, "Hotspot agregado", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    LaunchedEffect(state.selectedLatLng) {
        state.selectedLatLng?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 15f)
            )
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Agregar Hotspot", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it, placesClient) },
                placeholder = { Text("Buscar lugar...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            val selectedGroupName = mapViewModel.state.myGroups
                .firstOrNull { it.id == mapViewModel.state.selectedGroupId }
                ?.name

            Text(
                text = selectedGroupName?.let { "Grupo activo: $it" }
                    ?: "No hay grupo activo seleccionado",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            state.suggestions.forEach { (placeId, description) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.onSuggestionSelected(placeId, description, placesClient) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = description,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }

            if (state.showMap && state.selectedLatLng != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    ) {
                        Marker(
                            state = MarkerState(position = state.selectedLatLng!!),
                            title = state.selectedName
                        )
                    }
                }

                Text(text = state.selectedName, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                if (state.selectedAddress.isNotEmpty()) {
                    Text(
                        text = state.selectedAddress,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Button(
                    onClick = { viewModel.saveHotspot(mapViewModel.state.selectedGroupId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Agregar Hotspot")
                }
            }
        }
    }
}