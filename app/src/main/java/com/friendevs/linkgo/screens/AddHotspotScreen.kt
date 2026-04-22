package com.friendevs.linkgo.screens

import android.content.Context
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.friendevs.linkgo.data.saveHotspot
import com.friendevs.linkgo.model.Hotspot
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AddHotspotState(
    val searchQuery: String = "",
    val suggestions: List<Pair<String, String>> = emptyList(),
    val selectedLatLng: LatLng? = null,
    val selectedName: String = "",
    val selectedAddress: String = "",
    val showMap: Boolean = false,
    val hotspotSaved: Boolean = false,
    val errorMessage: String? = null
)

class AddHotspotViewModel : ViewModel() {

    var state by mutableStateOf(AddHotspotState())
        private set

    fun onSearchQueryChange(query: String, placesClient: PlacesClient) {
        state = state.copy(searchQuery = query, suggestions = emptyList())

        if (query.length >= 3) {
            viewModelScope.launch {
                try {
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(query)
                        .build()
                    val response = placesClient
                        .findAutocompletePredictions(request)
                        .await()
                    state = state.copy(
                        suggestions = response.autocompletePredictions.map {
                            Pair(it.placeId, it.getFullText(null).toString())
                        }
                    )
                } catch (e: Exception) {
                    state = state.copy(suggestions = emptyList())
                }
            }
        }
    }

    fun onSuggestionSelected(placeId: String, description: String, placesClient: PlacesClient) {
        viewModelScope.launch {
            try {
                val placeFields = listOf(
                    Place.Field.LAT_LNG,
                    Place.Field.NAME,
                    Place.Field.ADDRESS
                )
                val fetchRequest = FetchPlaceRequest.newInstance(placeId, placeFields)
                val placeResponse = placesClient.fetchPlace(fetchRequest).await()
                val place = placeResponse.place

                state = state.copy(
                    selectedLatLng = place.latLng,
                    selectedName = place.name ?: description,
                    selectedAddress = place.address ?: "",
                    searchQuery = place.name ?: description,
                    suggestions = emptyList(),
                    showMap = true
                )
            } catch (e: Exception) {
                state = state.copy(errorMessage = "Error al obtener lugar")
            }
        }
    }

    fun saveHotspot(context: Context) {
        val latlng = state.selectedLatLng ?: return
        val newHotspot = Hotspot(
            id = System.currentTimeMillis().toInt(),
            name = state.selectedName,
            lat = latlng.latitude,
            lng = latlng.longitude,
            fotos = 0,
            url = "",
            address = state.selectedAddress
        )
        saveHotspot(context, newHotspot)
        state = state.copy(hotspotSaved = true)
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHotspotScreen(
    navController: NavController,
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
                    onClick = { viewModel.saveHotspot(context) },
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