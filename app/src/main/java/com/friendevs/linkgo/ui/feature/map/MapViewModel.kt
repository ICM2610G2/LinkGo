package com.friendevs.linkgo.ui.feature.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.friendevs.linkgo.data.repository.FirebaseHotspotRepository
import com.friendevs.linkgo.data.repository.GroupRepository
import com.friendevs.linkgo.data.repository.LocationRepository
import com.friendevs.linkgo.domain.model.GroupSummary
import com.friendevs.linkgo.domain.model.Hotspot
import com.friendevs.linkgo.domain.model.UserLocation
import com.friendevs.linkgo.ui.feature.routes.fetchLinkGoRoute
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MapState(
    val locationPermissionGranted: Boolean = false,
    val showDialog: Boolean = true,
    val firstLocationUpdate: Boolean = true,
    val hotspots: List<Hotspot> = emptyList(),
    val currentUserLocation: LatLng? = null,
    val routePoints: List<LatLng> = emptyList(),
    val routeDistance: String = "",
    val routeEta: String = "",
    val isRouteLoading: Boolean = false,
    val routeError: String? = null,
    val pendingHotspot: Hotspot? = null,
    val myGroups: List<GroupSummary> = emptyList(),
    val selectedGroupId: String? = null,
    val groupMemberLocations: List<UserLocation> = emptyList()
)

class MapViewModel : ViewModel() {

    var state by mutableStateOf(MapState())
        private set

    private val groupRepo = GroupRepository()
    private val locationRepo = LocationRepository()

    private var myUid: String? = null
    private var allLocations: Map<String, UserLocation> = emptyMap()

    fun loadHotSpots(userId: String) {
        val repo = FirebaseHotspotRepository()

        repo.getHotspotsByUser(userId) { hotspots ->
            state = state.copy(hotspots = hotspots)
        }
    }

    /** Escucha los grupos del usuario y las ubicaciones de todos los usuarios. */
    fun observeGroupsAndLocations(uid: String) {
        myUid = uid

        groupRepo.observeMyGroups(uid) { groups ->
            // selecciona el primer grupo por defecto si aun no hay ninguno
            val selected = state.selectedGroupId ?: groups.firstOrNull()?.id
            state = state.copy(myGroups = groups, selectedGroupId = selected)
            recomputeMemberLocations()
        }

        locationRepo.observeAllLocations { locations ->
            allLocations = locations
            recomputeMemberLocations()
        }
    }

    fun selectGroup(groupId: String) {
        state = state.copy(selectedGroupId = groupId)
        recomputeMemberLocations()
    }

    /** Filtra allLocations a los miembros del grupo seleccionado, excluyendo al usuario actual. */
    private fun recomputeMemberLocations() {
        val group = state.myGroups.firstOrNull { it.id == state.selectedGroupId }
        if (group == null) {
            state = state.copy(groupMemberLocations = emptyList())
            return
        }

        val memberUids = group.members.filterValues { it }.keys
        val locations = memberUids
            .filter { it != myUid }
            .mapNotNull { allLocations[it] }

        state = state.copy(groupMemberLocations = locations)
    }

    /** Escribe la posicion del usuario actual en Realtime Database. */
    fun publishMyLocation(lat: Double, lng: Double) {
        val uid = myUid ?: return
        locationRepo.writeLocation(uid, lat, lng)
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

    fun onUserLocationUpdate(location: LatLng) {
        state = state.copy(currentUserLocation = location)

        val pending = state.pendingHotspot
        if (pending != null) {
            calculateRouteToHotspot(pending)
            state = state.copy(pendingHotspot = null)
        }
    }

    fun calculateRouteToHotspot(hotspot: Hotspot) {
        val origin = state.currentUserLocation
        if (origin == null) {
            state = state.copy(routeError = "No se pudo obtener tu ubicacion actual")
            return
        }

        val destination = LatLng(hotspot.lat, hotspot.lng)

        viewModelScope.launch {
            state = state.copy(isRouteLoading = true, routeError = null)

            runCatching {
                withContext(Dispatchers.IO) {
                    fetchLinkGoRoute(origin = origin, destination = destination)
                }
            }.onSuccess { route ->
                state = state.copy(
                    routePoints = route.points,
                    routeDistance = route.distanceText,
                    routeEta = route.durationText,
                    isRouteLoading = false
                )
            }.onFailure {
                state = state.copy(
                    isRouteLoading = false,
                    routeError = it.message ?: "No se pudo calcular la ruta"
                )
            }
        }
    }

    fun clearRouteError() {
        state = state.copy(routeError = null)
    }

    fun clearRoute() {
        state = state.copy(
            routePoints = emptyList(),
            routeDistance = "",
            routeEta = "",
            isRouteLoading = false,
            routeError = null
        )
    }

    //Esta funcion es un placeholder para la implementacion de MeetUp con grupos

    fun pickRandomHotspot() {
        val hotspots = state.hotspots

        if (hotspots.isEmpty()) {
            state = state.copy(routeError = "No hay hotspots disponibles")
            return
        }

        val randomHotspot = hotspots.random()

        state = state.copy(pendingHotspot = randomHotspot)
    }
}
