package com.friendevs.linkgo.ui.feature.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.friendevs.linkgo.domain.model.UserLocation
import com.friendevs.linkgo.ui.feature.routes.RouteInfoCard
import com.friendevs.linkgo.ui.navigation.Screens
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    navController: NavController,
    viewModel: MapViewModel,
    sensorViewModel: com.friendevs.linkgo.model.SensorViewModel
) {
    val context = LocalContext.current
    val state = viewModel.state
    val cameraPositionState = rememberCameraPositionState()
    var groupsExpanded by remember { mutableStateOf(false) }

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
            viewModel.loadHotSpots()
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
                    val meetupRoute = state.meetupRoutes.firstOrNull { it.uid == member.uid }
                    val markerIcon = rememberGroupMemberMarkerIcon(member)

                    Marker(
                        state = MarkerState(position = LatLng(member.lat, member.lng)),
                        title = member.name,
                        snippet = meetupRoute?.let {
                            "${it.distanceText} • ${it.durationText} al meetup"
                        } ?: "Miembro del grupo",
                        icon = markerIcon
                    )
                }

                state.meetupRoutes.forEach { memberRoute ->
                    Polyline(
                        points = memberRoute.points,
                        color = MaterialTheme.colorScheme.tertiary,
                        width = 8f
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

            ExposedDropdownMenuBox(
                expanded = groupsExpanded,
                onExpandedChange = { if (state.myGroups.isNotEmpty()) groupsExpanded = !groupsExpanded },
                modifier = Modifier
                    .padding(top = 65.dp)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = state.myGroups.firstOrNull { it.id == state.selectedGroupId }?.name ?: "Sin grupos",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Grupo activo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupsExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start)
                )

                ExposedDropdownMenu(
                    expanded = groupsExpanded,
                    onDismissRequest = { groupsExpanded = false }
                ) {
                    state.myGroups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.name.ifBlank { "Grupo sin nombre" }) },
                            onClick = {
                                viewModel.selectGroup(group.id)
                                groupsExpanded = false
                            }
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

            if (state.isMeetupLoading || state.meetupRoutes.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 190.dp)
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = state.meetupTargetHotspot?.let { "MeetUp: ${it.name}" } ?: "MeetUp",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (state.isMeetupLoading) {
                            Text(
                                text = "Calculando rutas para miembros del grupo...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            state.meetupRoutes.take(4).forEach { route ->
                                Text(
                                    text = "${route.name}: ${route.distanceText} • ${route.durationText}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (state.meetupRoutes.size > 4) {
                                Text(
                                    text = "+${state.meetupRoutes.size - 4} miembros",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
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

@Composable
private fun rememberGroupMemberMarkerIcon(member: UserLocation): BitmapDescriptor {
    val fallbackColor = remember(member.name) {
        val hue = (member.name.hashCode().and(0xFF)) / 255f * 360f
        Color.hsl(hue, 0.45f, 0.50f)
    }
    val initial = member.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
    val borderColor = MaterialTheme.colorScheme.primary
    val density = LocalDensity.current
    val sizePx = with(density) { 52.dp.toPx().toInt() }
    val borderPx = with(density) { 3.dp.toPx().toInt() }

    val fallbackIcon = remember(member.name, fallbackColor, borderColor, sizePx, borderPx) {
        createGroupMemberMarkerIcon(
            photoBitmap = null,
            initial = initial,
            fillColor = fallbackColor.toArgb(),
            borderColor = borderColor.toArgb(),
            sizePx = sizePx,
            borderPx = borderPx
        )
    }

    val icon by produceState(fallbackIcon, member.profilePhotoUrl, member.name, sizePx, borderPx) {
        val url = member.profilePhotoUrl.takeIf { it.isNotBlank() } ?: return@produceState
        value = withContext(Dispatchers.IO) {
            val bitmap = loadBitmapFromUrl(url)
            createGroupMemberMarkerIcon(
                photoBitmap = bitmap,
                initial = initial,
                fillColor = fallbackColor.toArgb(),
                borderColor = borderColor.toArgb(),
                sizePx = sizePx,
                borderPx = borderPx
            )
        }
    }

    return icon
}

private fun createGroupMemberMarkerIcon(
    photoBitmap: Bitmap?,
    initial: String,
    fillColor: Int,
    borderColor: Int,
    sizePx: Int,
    borderPx: Int
): BitmapDescriptor {
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = sizePx / 2f
    val radius = sizePx / 2f
    val innerRadius = radius - borderPx

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = borderColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, radius, borderPaint)

    if (photoBitmap != null) {
        val scaled = Bitmap.createScaledBitmap(photoBitmap, (innerRadius * 2).toInt(), (innerRadius * 2).toInt(), true)
        val shader = BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader }
        canvas.drawCircle(center, center, innerRadius, imagePaint)
    } else {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fillColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, innerRadius, fillPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = innerRadius
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textY = center - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(initial, center, textY, textPaint)
    }

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun loadBitmapFromUrl(url: String): Bitmap? {
    return runCatching {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            doInput = true
        }
        try {
            connection.connect()
            connection.inputStream.use { BitmapFactory.decodeStream(it) }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()
}

