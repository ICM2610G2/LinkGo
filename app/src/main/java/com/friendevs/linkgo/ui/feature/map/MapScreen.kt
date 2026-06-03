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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.friendevs.linkgo.domain.model.UserLocation
import com.friendevs.linkgo.ui.feature.routes.RouteInfoCard
import com.friendevs.linkgo.ui.navigation.Screens
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()

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
        // Re-centrar la camara en el usuario cada vez que se entra al mapa.
        viewModel.onCenterLocation()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            viewModel.loadHotSpots()
            viewModel.observeGroupsAndLocations(userId)
            viewModel.loadFeedPosts()
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
                5000L
            )
                .setMinUpdateIntervalMillis(2000L)
                .setMinUpdateDistanceMeters(10f)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return
                    val userLatLng = LatLng(location.latitude, location.longitude)

                    viewModel.onUserLocationUpdate(userLatLng)
                    viewModel.publishMyLocation(location.latitude, location.longitude)

                    if (state.firstLocationUpdate) {
                        cameraPositionState.move(
                            CameraUpdateFactory.newLatLngZoom(userLatLng, 17f)
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

    LaunchedEffect(state.meetupTargetHotspot) {
        val target = state.meetupTargetHotspot ?: return@LaunchedEffect
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(LatLng(target.lat, target.lng), 17f)
        )
    }

    LaunchedEffect(state.firstLocationUpdate) {
        if (state.firstLocationUpdate && state.locationPermissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    viewModel.onUserLocationUpdate(userLatLng)
                    cameraPositionState.move(
                        CameraUpdateFactory.newLatLngZoom(userLatLng, 17f)
                    )
                    viewModel.onFirstLocationUpdated()
                }
            }
        }
    }

    val myUid = remember { FirebaseAuth.getInstance().currentUser?.uid }
    val myUserFromFirebase = state.allLocations[myUid]
    val myUserLocation = state.currentUserLocation?.let { loc ->
        UserLocation(
            uid = myUid ?: "",
            name = myUserFromFirebase?.name ?: "Tu",
            lat = loc.latitude,
            lng = loc.longitude,
            profilePhotoUrl = myUserFromFirebase?.profilePhotoUrl ?: ""
        )
    }
    var sheetExpanded by remember { mutableStateOf(true) }

    val statusInsetTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navInsetBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // Espacio para no quedar debajo del BottomNavBar propio de la app (~80dp) + inset.
    val bottomBarSpace = navInsetBottom + 88.dp

    Scaffold(contentWindowInsets = WindowInsets(0)) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                // Sube el logo/legal de Google por encima de las barras del sistema y el nav.
                contentPadding = PaddingValues(top = statusInsetTop, bottom = bottomBarSpace),
                properties = MapProperties(
                    isMyLocationEnabled = false,
                    mapStyleOptions = if (sensorViewModel.isDarkBySensor) darkStyle else null,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                    rotationGesturesEnabled = true
                )
            ) {
                state.hotspots.forEach { hotspot ->
                    val isMeetupTarget = hotspot.id == state.meetupTargetHotspot?.id
                    val creator = state.allLocations[hotspot.creatorId]
                    val icon = rememberHotspotMarkerIcon(creator, isMeetupTarget)
                    Marker(
                        state = MarkerState(position = LatLng(hotspot.lat, hotspot.lng)),
                        title = if (isMeetupTarget) "\uD83D\uDCCD ${hotspot.name}" else hotspot.name,
                        snippet = if (isMeetupTarget) "Destino del MeetUp" else hotspot.address,
                        icon = icon,
                        onClick = {
                            navController.navigate("${Screens.HotspotGallery.name}/${hotspot.id}/${hotspot.name.replace("/", "-")}")
                            true
                        }
                    )
                }

                state.groupMemberLocations.forEach { member ->
                    if (member.uid == myUid) return@forEach
                    key(member.uid) {
                        val meetupRoute = state.meetupRoutes.firstOrNull { it.uid == member.uid }
                        val markerIcon = rememberGroupMemberMarkerIcon(member)
                        val markerState = remember { MarkerState(position = LatLng(member.lat, member.lng)) }
                        LaunchedEffect(member.lat, member.lng) {
                            markerState.position = LatLng(member.lat, member.lng)
                        }

                        Marker(
                            state = markerState,
                            title = member.name,
                            snippet = meetupRoute?.let {
                                "${it.distanceText} • ${it.durationText} al meetup"
                            } ?: "Miembro del grupo",
                            icon = markerIcon
                        )
                    }
                }

                // My marker always on top
                myUserLocation?.let { me ->
                    val markerIcon = rememberGroupMemberMarkerIcon(me)
                    val markerState = remember { MarkerState(position = LatLng(me.lat, me.lng)) }
                    LaunchedEffect(me.lat, me.lng) {
                        markerState.position = LatLng(me.lat, me.lng)
                    }
                    Marker(
                        state = markerState,
                        title = "Tu",
                        icon = markerIcon,
                        zIndex = 100f
                    )
                }

                // Fotos del Feed ancladas en el mapa (estilo BeReal).
                state.feedPosts.forEach { post ->
                    key("feed_${post.id}") {
                        val icon = rememberFeedMarkerIcon(post.imageUrl)
                        val markerState = remember { MarkerState(position = LatLng(post.lat, post.lng)) }
                        Marker(
                            state = markerState,
                            title = post.authorName,
                            snippet = post.caption.ifBlank { "Foto del feed" },
                            icon = icon
                        )
                    }
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

            // Selector de grupos en pildoras (arriba).
            GroupSelectorBar(
                groups = state.myGroups,
                selectedId = state.selectedGroupId,
                onSelect = { viewModel.selectGroup(it) },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 10.dp, start = 12.dp, end = 12.dp)
            )

            // Brujula propia (4o sensor: magnetometro) — chip discreto.
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(start = 12.dp, top = 64.dp)
            ) {
                Text(
                    text = "${compassDirection(sensorViewModel.headingDeg)} ${sensorViewModel.headingDeg.toInt()}°",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Ruta a un hotspot (ETA) cuando esta activa.
            if (state.isRouteLoading || state.routePoints.isNotEmpty()) {
                RouteInfoCard(
                    eta = state.routeEta,
                    distance = state.routeDistance,
                    isLoading = state.isRouteLoading,
                    onClose = { viewModel.clearRoute() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(top = 70.dp, start = 16.dp, end = 16.dp)
                )
            }

            // FABs minimalistas (derecha), sobre el panel.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = bottomBarSpace + 140.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        val members = state.groupMemberLocations
                        when {
                            members.isEmpty() ->
                                Toast.makeText(context, "Sin miembros con ubicacion", Toast.LENGTH_SHORT).show()
                            members.size == 1 -> scope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(members[0].lat, members[0].lng), 17f
                                    )
                                )
                            }
                            else -> scope.launch {
                                val builder = LatLngBounds.builder()
                                members.forEach { builder.include(LatLng(it.lat, it.lng)) }
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngBounds(builder.build(), 140)
                                )
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Place, contentDescription = "Ver grupo")
                }

                FloatingActionButton(
                    onClick = { viewModel.onCenterLocation() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Centrar en mi")
                }
            }

            // Panel inferior deslizable con los miembros del grupo.
            MembersSheet(
                members = state.groupMemberLocations,
                myUid = myUid,
                myLocation = state.currentUserLocation,
                meetupRoutes = state.meetupRoutes,
                isMeetupActive = state.meetupRoutes.isNotEmpty() || state.isMeetupLoading,
                expanded = sheetExpanded,
                onToggle = { sheetExpanded = !sheetExpanded },
                onMemberClick = { member ->
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(LatLng(member.lat, member.lng), 17f)
                        )
                    }
                },
                onMeetUp = { navController.navigate(Screens.MeetUp.name) },
                onCancelMeetup = { viewModel.cancelMeetup() },
                bottomSpace = bottomBarSpace,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

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
private fun rememberFeedMarkerIcon(imageUrl: String): BitmapDescriptor? {
    val borderColor = MaterialTheme.colorScheme.tertiary
    val density = LocalDensity.current
    val sizePx = with(density) { 56.dp.toPx().toInt() }
    val borderPx = with(density) { 3.dp.toPx().toInt() }

    val icon by produceState<BitmapDescriptor?>(null, imageUrl, sizePx, borderPx) {
        val url = imageUrl.takeIf { it.isNotBlank() } ?: return@produceState
        value = withContext(Dispatchers.IO) {
            val bitmap = loadBitmapFromUrl(url) ?: return@withContext null
            createGroupMemberMarkerIcon(
                photoBitmap = bitmap,
                initial = "",
                fillColor = borderColor.toArgb(),
                borderColor = borderColor.toArgb(),
                sizePx = sizePx,
                borderPx = borderPx
            )
        }
    }
    return icon
}

private fun compassDirection(deg: Float): String {
    val dirs = listOf("N", "NE", "E", "SE", "S", "SO", "O", "NO")
    return dirs[(((deg % 360f) + 22.5f) / 45f).toInt() % 8]
}

@Composable
private fun GroupSelectorBar(
    groups: List<com.friendevs.linkgo.domain.model.GroupSummary>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (groups.isEmpty()) return
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groups.forEach { group ->
            val selected = group.id == selectedId
            Surface(
                shape = CircleShape,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                tonalElevation = 3.dp,
                modifier = Modifier.clickable { onSelect(group.id) }
            ) {
                Text(
                    text = "${group.name.ifBlank { "Grupo" }} · ${group.membersCount}",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MembersSheet(
    members: List<UserLocation>,
    myUid: String?,
    myLocation: LatLng?,
    meetupRoutes: List<MemberMeetupRoute>,
    isMeetupActive: Boolean,
    expanded: Boolean,
    onToggle: () -> Unit,
    onMemberClick: (UserLocation) -> Unit,
    onMeetUp: () -> Unit,
    onCancelMeetup: () -> Unit,
    bottomSpace: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val ordered = remember(members, myUid) {
        members.sortedByDescending { it.uid == myUid }
    }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = bottomSpace)
    ) {
        Column(modifier = Modifier.padding(bottom = 8.dp)) {
            // Handle: toggle colapsar/expandir.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "${members.size} miembros",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (isMeetupActive) {
                    TextButton(
                        onClick = onCancelMeetup,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Cancelar meetup")
                    }
                } else {
                    TextButton(onClick = onMeetUp) {
                        Icon(Icons.Default.Person, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Meet Up")
                    }
                }
            }

            if (expanded) {
                if (ordered.isEmpty()) {
                    Text(
                        text = "Sin miembros con ubicacion activa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(ordered, key = { it.uid }) { member ->
                            val route = meetupRoutes.firstOrNull { it.uid == member.uid }
                            MemberRow(
                                member = member,
                                isMe = member.uid == myUid,
                                subtitle = when {
                                    route != null -> "${route.distanceText} • ${route.durationText}"
                                    member.uid == myUid -> "Tu ubicacion"
                                    else -> formatDistanceTo(myLocation, member.lat, member.lng)
                                },
                                onClick = { onMemberClick(member) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: UserLocation,
    isMe: Boolean,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box {
            val initial = member.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
            if (member.profilePhotoUrl.isNotBlank()) {
                AsyncImage(
                    model = member.profilePhotoUrl,
                    contentDescription = member.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initial, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            // Punto de estado "activo".
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isMe) "Tu" else member.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatDistanceTo(me: LatLng?, lat: Double, lng: Double): String {
    if (me == null) return "Ubicacion compartida"
    val result = FloatArray(1)
    android.location.Location.distanceBetween(me.latitude, me.longitude, lat, lng, result)
    val meters = result[0]
    return if (meters < 1000f) "a ${meters.toInt()} m"
    else "a ${"%.1f".format(meters / 1000f)} km"
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

private val okHttpClient = okhttp3.OkHttpClient()

private fun loadBitmapFromUrl(url: String): Bitmap? {
    return runCatching {
        val request = okhttp3.Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute()
        if (response.isSuccessful) {
            response.body?.byteStream()?.use { BitmapFactory.decodeStream(it) }
        } else {
            null
        }
    }.onFailure { it.printStackTrace() }.getOrNull()
}

@Composable
private fun rememberHotspotMarkerIcon(creator: UserLocation?, isTarget: Boolean): BitmapDescriptor {
    val initial = creator?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "H"
    val photoUrl = creator?.profilePhotoUrl ?: ""
    val density = LocalDensity.current
    
    val sizePx = with(density) { 56.dp.toPx().toInt() }
    
    val icon by produceState<BitmapDescriptor?>(null, photoUrl, isTarget, sizePx) {
        value = withContext(Dispatchers.IO) {
            val bitmap = if (photoUrl.isNotBlank()) loadBitmapFromUrl(photoUrl) else null
            createHotspotMarkerIcon(bitmap, initial, isTarget, sizePx)
        }
    }
    
    return icon ?: BitmapDescriptorFactory.defaultMarker(if (isTarget) BitmapDescriptorFactory.HUE_AZURE else BitmapDescriptorFactory.HUE_ORANGE)
}

private fun createHotspotMarkerIcon(
    photoBitmap: Bitmap?,
    initial: String,
    isTarget: Boolean,
    sizePx: Int
): BitmapDescriptor {
    val width = sizePx
    val height = (sizePx * 1.4f).toInt()
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val centerX = width / 2f
    val radius = width / 2f
    val centerY = radius
    
    val borderColor = if (isTarget) android.graphics.Color.parseColor("#2196F3") else android.graphics.Color.parseColor("#FF9800")
    
    val path = android.graphics.Path().apply {
        addCircle(centerX, centerY, radius, android.graphics.Path.Direction.CW)
        moveTo(0f, centerY)
        lineTo(centerX, height.toFloat())
        lineTo(width.toFloat(), centerY)
        close()
    }
    
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = borderColor
        style = Paint.Style.FILL
    }
    canvas.drawPath(path, borderPaint)
    
    val borderPx = width / 12f
    val innerRadius = radius - borderPx
    
    if (photoBitmap != null) {
        val scaled = Bitmap.createScaledBitmap(photoBitmap, (innerRadius * 2).toInt(), (innerRadius * 2).toInt(), true)
        val shader = BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.shader = shader }
        canvas.drawCircle(centerX, centerY, innerRadius, imagePaint)
    } else {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, centerY, innerRadius, fillPaint)
        
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = borderColor
            textAlign = Paint.Align.CENTER
            textSize = innerRadius * 1.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textY = centerY - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(initial, centerX, textY, textPaint)
    }
    
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

