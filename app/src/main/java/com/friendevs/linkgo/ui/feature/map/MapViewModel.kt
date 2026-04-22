package com.friendevs.linkgo.ui.feature.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.friendevs.linkgo.data.repository.FirebaseHotspotRepository
import com.friendevs.linkgo.domain.model.Hotspot

data class MapState(
    val locationPermissionGranted: Boolean = false,
    val showDialog: Boolean = true,
    val firstLocationUpdate: Boolean = true,
    val hotspots: List<Hotspot> = emptyList()
)

class MapViewModel : ViewModel() {

    var state by mutableStateOf(MapState())
        private set

    fun loadHotSpots(userId: String) {
        val repo = FirebaseHotspotRepository()

        repo.getHotspotsByUser(userId) { hotspots ->
            state = state.copy(hotspots = hotspots)
        }
    }

    fun onPermissionResult(granted: Boolean) {
        state = state.copy(
            locationPermissionGranted = granted,
            showDialog = false
        )
    }

    fun onPermissionAlreadyGranted() {
        state = state.copy(
            locationPermissionGranted = true,
            showDialog = false
        )
    }

    fun onFirstLocationUpdated() {
        state = state.copy(firstLocationUpdate = false)
    }

    fun onCenterLocation() {
        state = state.copy(firstLocationUpdate = true)
    }
}
