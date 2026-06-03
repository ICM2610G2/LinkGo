package com.friendevs.linkgo.ui.feature.map

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.friendevs.linkgo.data.repository.FeedRepository
import com.friendevs.linkgo.data.repository.FirebaseHotspotRepository
import com.friendevs.linkgo.data.repository.GroupRepository
import com.friendevs.linkgo.data.repository.LocationRepository
import com.friendevs.linkgo.domain.model.FeedPost
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
    val allLocations: Map<String, UserLocation> = emptyMap(),
    val hotspots: List<Hotspot> = emptyList(),
    val currentUserLocation: LatLng? = null,
    val routePoints: List<LatLng> = emptyList(),
    val routeDistance: String = "",
    val routeEta: String = "",
    val isRouteLoading: Boolean = false,
    val isMeetupLoading: Boolean = false,
    val routeError: String? = null,
    val pendingHotspot: Hotspot? = null,
    val myGroups: List<GroupSummary> = emptyList(),
    val selectedGroupId: String? = null,
    val groupMemberLocations: List<UserLocation> = emptyList(),
    val meetupTargetHotspot: Hotspot? = null,
    val meetupRoutes: List<MemberMeetupRoute> = emptyList(),
    val feedPosts: List<FeedPost> = emptyList(),
    val selectedMemberUidsForMeetup: Set<String> = emptySet()
)

data class MemberMeetupRoute(
    val uid: String,
    val name: String,
    val points: List<LatLng>,
    val distanceText: String,
    val durationText: String
)

class MapViewModel : ViewModel() {

    var state by mutableStateOf(MapState())
        private set

    private val groupRepo = GroupRepository()
    private val locationRepo = LocationRepository()
    private val feedRepo = FeedRepository()

    private var myUid: String? = null
    private var allLocations: Map<String, UserLocation> = emptyMap()
    private var allHotspots: List<Hotspot> = emptyList()
    private var hotspotsObserved = false
    private var groupsAndLocationsObserved = false
    private var feedObserved = false
    private var lastMeetupRecalcLocation: LatLng? = null
    private var isMeetupRouteRecalculating = false
    private val MEETUP_RECALC_DISTANCE_METERS = 30f   // recalcular si el usuario se mueve >= 30 m
    private var meetupRoutesListener: com.google.firebase.database.ValueEventListener? = null
    private var meetupRoutesGroupId: String? = null

    fun loadFeedPosts() {
        if (feedObserved) return
        feedObserved = true
        feedRepo.observeFeed { posts ->
            // Solo posts con ubicacion valida para anclarlos en el mapa.
            state = state.copy(feedPosts = posts.filter { it.lat != 0.0 || it.lng != 0.0 })
        }
    }

    fun loadHotSpots() {
        if (hotspotsObserved) return
        hotspotsObserved = true

        val repo = FirebaseHotspotRepository()

        repo.getHotspots { hotspots ->
            allHotspots = hotspots
            recomputeVisibleHotspots()
        }
    }

    /** Escucha los grupos del usuario y las ubicaciones de todos los usuarios. */
    fun observeGroupsAndLocations(uid: String) {
        if (groupsAndLocationsObserved) {
            myUid = uid
            return
        }
        groupsAndLocationsObserved = true
        myUid = uid

        Log.d("MapViewModel", "observeGroupsAndLocations: Starting for myUid=$uid")

        groupRepo.observeMyGroups(uid) { groups ->
            Log.d("MapViewModel", "observeMyGroups callback: ${groups.size} groups")
            groups.forEach { group ->
                Log.d("MapViewModel", "  Group: id=${group.id}, name=${group.name}, members=${group.members.size}")
                group.members.forEach { (memberId, isActive) ->
                    Log.d("MapViewModel", "    - Member: $memberId = $isActive")
                }
            }

            val hasCurrentSelection = groups.any { it.id == state.selectedGroupId }
            val selected = if (hasCurrentSelection) state.selectedGroupId else groups.firstOrNull()?.id
            state = state.copy(myGroups = groups, selectedGroupId = selected)
            Log.d("MapViewModel", "observeMyGroups: selectedGroupId=$selected")
            recomputeMemberLocations()
            recomputeVisibleHotspots()
        }

        locationRepo.observeAllLocations { locations ->
            Log.d("MapViewModel", "observeAllLocations callback: ${locations.size} locations received")
            locations.forEach { (uid, location) ->
                Log.d("MapViewModel", "  Location: uid=$uid, name=${location.name}, lat=${location.lat}, lng=${location.lng}")
            }

            allLocations = locations
            state = state.copy(allLocations = locations)
            Log.d("MapViewModel", "Updated allLocations: now has ${allLocations.size} entries")
            recomputeMemberLocations()
        }
    }

    fun selectGroup(groupId: String) {
        state = state.copy(
            selectedGroupId = groupId,
            meetupTargetHotspot = null,
            meetupRoutes = emptyList(),
            isMeetupLoading = false,
            selectedMemberUidsForMeetup = emptySet()
        )
        recomputeMemberLocations()
        recomputeVisibleHotspots()
    }

    fun cancelMeetup() {
        state = state.copy(
            meetupTargetHotspot = null,
            meetupRoutes = emptyList(),
            isMeetupLoading = false
        )
        lastMeetupRecalcLocation = null
        isMeetupRouteRecalculating = false
        stopObservingMeetupRoutes()
        meetupRoutesGroupId?.let { gid ->
            myUid?.let { uid -> locationRepo.deleteMeetupRoute(gid, uid) }
        }
        meetupRoutesGroupId = null
    }

    private fun startObservingMeetupRoutes(groupId: String) {
        stopObservingMeetupRoutes()
        meetupRoutesGroupId = groupId
        meetupRoutesListener = locationRepo.observeMeetupRoutes(groupId) { remoteRoutes ->
            val updatedRoutes = state.meetupRoutes.map { existing ->
                val remote = remoteRoutes[existing.uid] ?: return@map existing
                val (encodedPoints, dist, dur) = remote
                val points = encodedPoints.mapNotNull { s ->
                    val parts = s.split(",")
                    if (parts.size == 2) {
                        val lat = parts[0].toDoubleOrNull()
                        val lng = parts[1].toDoubleOrNull()
                        if (lat != null && lng != null) com.google.android.gms.maps.model.LatLng(lat, lng) else null
                    } else null
                }
                existing.copy(points = points, distanceText = dist, durationText = dur)
            }
            state = state.copy(meetupRoutes = updatedRoutes)
        }
    }

    private fun stopObservingMeetupRoutes() {
        val gid = meetupRoutesGroupId ?: return
        val listener = meetupRoutesListener ?: return
        locationRepo.removeMeetupRoutesListener(gid, listener)
        meetupRoutesListener = null
    }

    private fun publishMyMeetupRoute(route: MemberMeetupRoute) {
        val gid = meetupRoutesGroupId ?: return
        val uid = myUid ?: return
        val encoded = route.points.map { "${it.latitude},${it.longitude}" }
        locationRepo.writeMeetupRoute(gid, uid, encoded, route.distanceText, route.durationText)
    }

    fun toggleMemberForMeetup(uid: String) {
        val current = state.selectedMemberUidsForMeetup.ifEmpty {
            state.groupMemberLocations.map { it.uid }.toSet()
        }
        val updated = if (uid in current) current - uid else current + uid
        state = state.copy(selectedMemberUidsForMeetup = updated)
    }

    /** Filtra allLocations a los miembros del grupo seleccionado, INCLUYENDO al usuario actual si tiene ubicación. */
    private fun recomputeMemberLocations() {
        val group = state.myGroups.firstOrNull { it.id == state.selectedGroupId }
        Log.d("MapViewModel", "recomputeMemberLocations: selectedGroupId=${state.selectedGroupId}, foundGroup=${group != null}")

        if (group == null) {
            Log.d("MapViewModel", "  No group found, clearing groupMemberLocations")
            state = state.copy(groupMemberLocations = emptyList())
            return
        }

        val memberUids = group.members.filterValues { it }.keys
        Log.d("MapViewModel", "  Group members (active): ${memberUids.toList()}")
        Log.d("MapViewModel", "  Searching in allLocations: ${allLocations.keys.toList()}")
        Log.d("MapViewModel", "  myUid: $myUid, currentUserLocation: ${state.currentUserLocation}")

        val locations = memberUids.mapNotNull { uid ->
            if (uid == myUid) {
                // Usuario actual: siempre usar GPS local (sin lag de Firebase)
                val gps = state.currentUserLocation
                val fromFirebase = allLocations[uid]
                if (gps != null) {
                    Log.d("MapViewModel", "  ✓ GPS para usuario actual $uid: ${gps.latitude}, ${gps.longitude}")
                    UserLocation(
                        uid = uid,
                        name = fromFirebase?.name ?: "Tu",
                        lat = gps.latitude,
                        lng = gps.longitude,
                        profilePhotoUrl = fromFirebase?.profilePhotoUrl ?: ""
                    )
                } else if (fromFirebase != null) {
                    Log.d("MapViewModel", "  ✓ Firebase (sin GPS aún) para $uid")
                    fromFirebase
                } else {
                    Log.d("MapViewModel", "  ✗ Sin ubicación para usuario actual $uid")
                    null
                }
            } else {
                // Otros miembros: solo datos de Firebase
                val fromFirebase = allLocations[uid]
                if (fromFirebase != null) {
                    Log.d("MapViewModel", "  ✓ Firebase para $uid: ${fromFirebase.name}")
                    fromFirebase
                } else {
                    Log.d("MapViewModel", "  ✗ Sin ubicación Firebase para $uid")
                    null
                }
            }
        }

        Log.d("MapViewModel", "  Final result: ${locations.size} locations in groupMemberLocations")
        state = state.copy(groupMemberLocations = locations)
    }

    /** Filtra hotspots para mostrar solo los que pertenecen al grupo seleccionado. */
    private fun recomputeVisibleHotspots() {
        val group = state.myGroups.firstOrNull { it.id == state.selectedGroupId }
        if (group == null) {
            state = state.copy(hotspots = emptyList())
            return
        }

        val visibleHotspots = allHotspots.filter { it.groupId == group.id }
        state = state.copy(hotspots = visibleHotspots)
    }

    fun selectedGroupMembersForMeetup(): List<UserLocation> =
        state.groupMemberLocations

    fun startMeetupToNearestHotspot() {
        val selectedGroup = state.myGroups.firstOrNull { it.id == state.selectedGroupId }
        if (selectedGroup == null) {
            state = state.copy(routeError = "Selecciona un grupo para iniciar el MeetUp")
            return
        }

        if (state.hotspots.isEmpty()) {
            state = state.copy(routeError = "El grupo no tiene hotspots disponibles")
            return
        }

        val selectedUids = state.selectedMemberUidsForMeetup.ifEmpty {
            state.groupMemberLocations.map { it.uid }.toSet()
        }
        val members = state.groupMemberLocations.filter { it.uid in selectedUids }.toMutableList()
        val myUidSnapshot = myUid
        val myLocSnapshot = state.currentUserLocation
        if (myUidSnapshot != null && myLocSnapshot != null) {
            val idx = members.indexOfFirst { it.uid == myUidSnapshot }
            val myEntry = UserLocation(
                uid = myUidSnapshot,
                name = members.getOrNull(idx)?.name ?: "Tu",
                lat = myLocSnapshot.latitude,
                lng = myLocSnapshot.longitude,
                profilePhotoUrl = members.getOrNull(idx)?.profilePhotoUrl ?: ""
            )
            if (idx >= 0) members[idx] = myEntry else members.add(myEntry)
        }

        if (members.isEmpty()) {
            state = state.copy(routeError = "No hay ubicaciones activas de miembros en este grupo")
            return
        }

        // Hotspot mas cercano: minimiza la distancia media (Haversine) a todos los miembros.
        val targetHotspot = state.hotspots.minByOrNull { hotspot ->
            members.map { member ->
                val result = FloatArray(1)
                android.location.Location.distanceBetween(
                    member.lat, member.lng, hotspot.lat, hotspot.lng, result
                )
                result[0]
            }.average()
        } ?: state.hotspots.first()
        val destination = LatLng(targetHotspot.lat, targetHotspot.lng)

        viewModelScope.launch {
            lastMeetupRecalcLocation = state.currentUserLocation
            isMeetupRouteRecalculating = false
            state = state.copy(
                isMeetupLoading = true,
                routeError = null,
                meetupTargetHotspot = targetHotspot,
                meetupRoutes = emptyList()
            )

            val routes = mutableListOf<MemberMeetupRoute>()

            members.forEach { member ->
                val origin = LatLng(member.lat, member.lng)
                runCatching {
                    withContext(Dispatchers.IO) {
                        fetchLinkGoRoute(origin = origin, destination = destination)
                    }
                }.onSuccess { route ->
                    routes.add(
                        MemberMeetupRoute(
                            uid = member.uid,
                            name = member.name,
                            points = route.points,
                            distanceText = route.distanceText,
                            durationText = route.durationText
                        )
                    )
                }.onFailure { error ->
                    Log.w("MapViewModel", "Ruta fallida para ${member.name} (${member.uid}): ${error.message}")
                }
            }

            state = state.copy(
                isMeetupLoading = false,
                meetupRoutes = routes,
                routeError = if (routes.isEmpty()) "No se pudieron calcular rutas del grupo" else null
            )

            // Suscribirse a rutas remotas de otros miembros
            val gid = state.selectedGroupId
            if (gid != null && routes.isNotEmpty()) {
                startObservingMeetupRoutes(gid)
                // Publicar la ruta propia para que los demás la vean
                val myId = myUid
                routes.firstOrNull { it.uid == myId }?.let { publishMyMeetupRoute(it) }
            }
        }
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
        // Recalcula ubicaciones de miembros para incluir la ubicación actual del usuario
        recomputeMemberLocations()

        val pending = state.pendingHotspot
        if (pending != null) {
            calculateRouteToHotspot(pending)
            state = state.copy(pendingHotspot = null)
        }

        // Recalcular ruta propia del meetup si el usuario se movio lo suficiente
        val target = state.meetupTargetHotspot
        val myId = myUid
        if (target != null && myId != null && state.meetupRoutes.isNotEmpty() && !isMeetupRouteRecalculating) {
            val last = lastMeetupRecalcLocation
            val moved = if (last == null) {
                true
            } else {
                val result = FloatArray(1)
                android.location.Location.distanceBetween(
                    last.latitude, last.longitude,
                    location.latitude, location.longitude,
                    result
                )
                result[0] >= MEETUP_RECALC_DISTANCE_METERS
            }

            if (moved) {
                lastMeetupRecalcLocation = location
                isMeetupRouteRecalculating = true
                val destination = LatLng(target.lat, target.lng)
                viewModelScope.launch {
                    runCatching {
                        withContext(Dispatchers.IO) {
                            fetchLinkGoRoute(origin = location, destination = destination)
                        }
                    }.onSuccess { route ->
                        val newEntry = MemberMeetupRoute(
                            uid = myId,
                            name = state.meetupRoutes.firstOrNull { it.uid == myId }?.name ?: "Tu",
                            points = route.points,
                            distanceText = route.distanceText,
                            durationText = route.durationText
                        )
                        val updatedRoutes = state.meetupRoutes.map { existing ->
                            if (existing.uid == myId) newEntry else existing
                        }
                        state = state.copy(meetupRoutes = updatedRoutes)
                        // Publicar en Firebase para que los demás vean la ruta actualizada
                        publishMyMeetupRoute(newEntry)
                    }.onFailure { error ->
                        Log.w("MapViewModel", "Recalculo de ruta meetup fallido: ${error.message}")
                    }
                    isMeetupRouteRecalculating = false
                }
            }
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

    fun pickNearestHotspot() {
        startMeetupToNearestHotspot()
    }

    override fun onCleared() {
        super.onCleared()
        stopObservingMeetupRoutes()
    }
}