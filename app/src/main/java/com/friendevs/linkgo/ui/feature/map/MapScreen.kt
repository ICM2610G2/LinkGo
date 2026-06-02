package com.friendevs.linkgo.ui.feature.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.friendevs.linkgo.ui.feature.routes.RouteInfoCard
import com.friendevs.linkgo.ui.navigation.Screens
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*

@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel = viewModel(),
    sensorViewModel: com.friendevs.linkgo.model.SensorViewModel
) {
    val context = LocalContext.current
    val state = viewModel.state
    val cameraPositionState = rememberCameraPositionState()

    val darkStyle = remember {
        com.google.android.gms.maps.model.MapStyleOptions.loadRawResourceStyle(
            context,
            com.friendevs.linkgo.R.raw.map_style_dark
        )
    }

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.loadHotSpots(userId)
            viewModel.observeGroupsAndLocations(userId)
        }
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.onPermissionAlreadyGranted()
        }
    }

    LaunchedEffect(state.routeError) {
        state.routeError?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearRouteError()
        }
    }

    LaunchedEffect(state.locationPermissionGranted) {
        if (state.locationPermissionGranted) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                Long.MAX_VALUE
            )
                .setMinUpdateDistanceMeters(30f)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
                    val userLatLng = LatLng(location.latitude, location.longitude)

                    viewModel.onUserLocationUpdate(userLatLng)
                    viewModel.publishMyLocation(location.latitude, location.longitude)

                    if (state.firstLocationUpdate) {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                        )
                        viewModel.onFirstLocationUpdated()
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    LaunchedEffect(state.firstLocationUpdate) {
        if (state.firstLocationUpdate && state.locationPermissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    viewModel.onUserLocationUpdate(userLatLng)
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(userLatLng, 15f)
                    )
                    viewModel.onFirstLocationUpdated()
                }
            }
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = state.locationPermissionGranted,
                    mapStyleOptions = if (sensorViewModel.isDarkBySensor) darkStyle else null,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(myLocationButtonEnabled = true)
            ) {
                state.hotspots.forEach { hotspot ->
                    Marker(
                        state = MarkerState(position = LatLng(hotspot.lat, hotspot.lng)),
                        title = hotspot.name,
                        snippet = hotspot.address,
                        onClick = {
                            viewModel.calculateRouteToHotspot(hotspot)
                            true
                        }
                    )
                }

                state.groupMemberLocations.forEach { member ->
                    Marker(
                        state = MarkerState(position = LatLng(member.lat, member.lng)),
                        title = member.name,
                        snippet = "Miembro del grupo"
                    )
                }

                if (state.routePoints.isNotEmpty()) {
                    Polyline(
                        points = state.routePoints,
                        color = MaterialTheme.colorScheme.primary,
                        width = 12f
                    )
                }
            }

            Row(
                modifier = Modifier
                    .padding(top = 65.dp)
                    .align(Alignment.TopCenter)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.myGroups.isEmpty()) {
                    Text(
                        text = "Sin grupos",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                } else {
                    state.myGroups.forEach { group ->
                        val selected = group.id == state.selectedGroupId
                        Text(
                            text = group.name,
                            textAlign = TextAlign.Center,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .background(
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surface,
                                    CircleShape
                                )
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .clickable { viewModel.selectGroup(group.id) }
                        )
                    }
                }
            }

            if (state.isRouteLoading || state.routePoints.isNotEmpty()) {
                RouteInfoCard(
                    eta = state.routeEta,
                    distance = state.routeDistance,
                    isLoading = state.isRouteLoading,
                    onClose = { viewModel.clearRoute() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 120.dp)
                        .padding(horizontal = 16.dp)
                )
            }

            Button(
                onClick = { navController.navigate(Screens.MeetUp.name) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, bottom = 35.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.width(5.dp))
                    Text("Meet Up")
                }
            }

            Button(
                onClick = { viewModel.onCenterLocation() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 35.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
            }

            if (state.showDialog && !state.locationPermissionGranted) {
                AlertDialog(
                    onDismissRequest = {},
                    confirmButton = {
                        Button(onClick = {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }) {
                            Text("Permitir")
                        }
                    },
                    title = { Text("Ubicación requerida") },
                    text = {
                        Text("Necesitamos tu ubicación en tiempo real para mostrarte en el mapa y ver hotspots cercanos.")
                    }
                )
            }
        }
    }
}
