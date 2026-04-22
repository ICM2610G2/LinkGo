package com.friendevs.linkgo.ui.feature.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendevs.linkgo.data.repository.loadHotspots
import com.friendevs.linkgo.domain.model.Hotspot
import kotlinx.coroutines.launch

data class MapState(
    val locationPermissionGranted: Boolean = false,
    val showDialog: Boolean = true,
    val firstLocationUpdate: Boolean = true,
    val hotspots: List<Hotspot> = emptyList()
)

class MapViewModel : ViewModel() {

    var state by mutableStateOf(MapState())
        private set

    fun loadHotSpots(context: android.content.Context) {
        viewModelScope.launch {
            state = state.copy(hotspots = loadHotspots(context))
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
