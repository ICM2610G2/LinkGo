package com.friendevs.linkgo.ui.feature.feed

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.friendevs.linkgo.R
import com.friendevs.linkgo.domain.model.FeedPost
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FeedScreen(
    navController: NavController,
    sensorViewModel: com.friendevs.linkgo.model.SensorViewModel = viewModel(),
    viewModel: FeedViewModel = viewModel()
) {
    val context = LocalContext.current
    val state = viewModel.state

    androidx.compose.runtime.LaunchedEffect(sensorViewModel.shakeDetected) {
        if (sensorViewModel.shakeDetected) {
            sensorViewModel.resetShake()
        }
    }

    androidx.compose.runtime.LaunchedEffect(state.error) {
        if (state.error != null) {
            android.widget.Toast.makeText(context, state.error, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    var showCaption by remember { mutableStateOf(false) }

    val cameraPermission = Manifest.permission.CAMERA
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, cameraPermission) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            bitmapToCacheUri(context, bitmap)?.let { uri ->
                pendingUri = uri
                caption = ""
                showCaption = true
            }
        }
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) cameraLauncher.launch(null)
    }

    val launchCamera = {
        if (hasCameraPermission) cameraLauncher.launch(null)
        else requestCameraPermission.launch(cameraPermission)
    }

    Scaffold(
        topBar = { TopBarFeed(onCameraClick = launchCamera, isUploading = state.isUploading) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxSize()
        ) {
            // Filtros: Todos / Mis circulos
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    val isAllSelected = state.filter == FeedFilter.ALL
                    FilterChip("Todos", isAllSelected) { viewModel.setFilter(FeedFilter.ALL) }
                }
                items(state.myGroups) { group ->
                    val isSelected = state.filter == FeedFilter.CIRCLE && state.selectedGroupId == group.id
                    FilterChip(group.name, isSelected) {
                        viewModel.selectGroup(group.id)
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (!state.hasRevealed) Modifier.blur(18.dp) else Modifier),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.posts) { post ->
                        FeedPostCard(
                            post = post,
                            currentUserId = viewModel.currentUserId,
                            onLikeClick = { viewModel.toggleLike(post.id) }
                        )
                    }
                }

                // Gating estilo BeReal: hasta postear no se revela el feed.
                if (!state.hasRevealed) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
                            .clickable { launchCamera() },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.photo_camera),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Toma tu foto del día para ver el feed",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 12.dp, start = 32.dp, end = 32.dp)
                        )
                    }
                }
            }
        }
    }

    if (showCaption && pendingUri != null) {
        AlertDialog(
            onDismissRequest = { showCaption = false },
            title = { Text("Comparte tu momento") },
            text = {
                Column {
                    AsyncImage(
                        model = pendingUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Descripción (opcional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingUri?.let { viewModel.publishPhoto(it, caption) }
                    showCaption = false
                }) { Text("Publicar") }
            },
            dismissButton = {
                TextButton(onClick = { showCaption = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text = label,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
                shape = CircleShape
            )
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clickable { onClick() }
    )
}

@Composable
private fun FeedPostCard(post: FeedPost, currentUserId: String?, onLikeClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (post.authorPhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model = post.authorPhotoUrl,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = post.authorName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatTimestamp(post.timestamp),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Imagen del post",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            if (post.caption.isNotBlank()) {
                Text(
                    text = post.caption,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            PostActions(post = post, currentUserId = currentUserId, onLikeClick = onLikeClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarFeed(onCameraClick: () -> Unit = {}, isUploading: Boolean = false) {
    TopAppBar(
        title = {
            Text(
                text = "Feed",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onCameraClick,
                enabled = !isUploading,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.photo_camera),
                        contentDescription = "Cámara",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun PostActions(post: FeedPost, currentUserId: String?, onLikeClick: () -> Unit) {
    val liked = currentUserId != null && post.likedBy[currentUserId] == true
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onLikeClick) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Like",
                tint = if (liked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
            )
        }
        val likesText = when {
            post.likesCount == 0 -> "Sé el primero en dar me gusta"
            post.likesCount == 1 && liked -> "Te gusta"
            post.likesCount == 1 && !liked -> "1 me gusta"
            liked -> "A ti y a ${post.likesCount - 1} más les gusta"
            else -> "${post.likesCount} me gusta"
        }
        Text(text = likesText, fontSize = 14.sp)
    }
}

private fun formatTimestamp(ts: Long): String {
    if (ts <= 0L) return ""
    return SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(ts))
}

private fun bitmapToCacheUri(context: Context, bitmap: Bitmap): Uri? {
    return runCatching {
        val file = File(context.cacheDir, "feed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        file.toUri()
    }.getOrNull()
}
