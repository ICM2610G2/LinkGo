package com.friendevs.linkgo.ui.feature.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendevs.linkgo.data.repository.FirebaseHotspotRepository
import com.friendevs.linkgo.domain.model.Hotspot
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
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

    fun saveHotspot(groupId: String?) {
        val latlng = state.selectedLatLng ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val selectedGroupId = groupId?.takeIf { it.isNotBlank() }
        if (selectedGroupId == null) {
            state = state.copy(errorMessage = "Selecciona un grupo activo antes de agregar hotspot")
            return
        }

        val newHotspot = Hotspot(
            id = "",
            name = state.selectedName,
            lat = latlng.latitude,
            lng = latlng.longitude,
            fotos = 0,
            url = "",
            address = state.selectedAddress,
            groupId = selectedGroupId,
            creatorId = userId
        )
        val repo = FirebaseHotspotRepository()

        repo.saveHotspot(userId, selectedGroupId, newHotspot)
        state = state.copy(hotspotSaved = true)
    }

    fun clearError() {
        state = state.copy(errorMessage = null)
    }
}
