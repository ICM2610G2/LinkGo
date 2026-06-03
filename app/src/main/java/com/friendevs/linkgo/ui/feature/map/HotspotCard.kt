package com.friendevs.linkgo.ui.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.friendevs.linkgo.domain.model.Hotspot
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import coil3.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun HotspotCard(hotspot: Hotspot, onClick: () -> Unit = {}) {
    var firstPhotoUrl by remember { mutableStateOf<String?>(null) }
    var isLoadingPhoto by remember { mutableStateOf(false) }

    LaunchedEffect(hotspot.id) {
        if (hotspot.fotos > 0) {
            isLoadingPhoto = true
            try {
                val storage = FirebaseStorage.getInstance().reference
                val items = storage
                    .child("hotspots/${hotspot.id}")
                    .listAll()
                    .await()
                    .items
                    .sortedByDescending { it.name }

                if (items.isNotEmpty()) {
                    val url = items.first().downloadUrl.await().toString()
                    firstPhotoUrl = url
                }
            } catch (e: Exception) {
                firstPhotoUrl = null
            } finally {
                isLoadingPhoto = false
            }
        }
    }

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = hotspot.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Fotos: ${hotspot.fotos}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoadingPhoto -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                    firstPhotoUrl != null -> {
                        AsyncImage(
                            model = firstPhotoUrl,
                            contentDescription = hotspot.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    }
                    else -> {
                        Text(
                            text = "Sin fotos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}