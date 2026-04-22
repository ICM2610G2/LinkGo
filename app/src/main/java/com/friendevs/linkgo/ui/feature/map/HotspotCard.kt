package com.friendevs.linkgo.ui.feature.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.friendevs.linkgo.domain.model.Hotspot
import androidx.compose.ui.graphics.Color
import coil3.compose.AsyncImage

@Composable
fun HotspotCard(hotspot: Hotspot) {

    ElevatedCard(
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
            AsyncImage(
                model = hotspot.url,
                contentDescription = hotspot.name,
                modifier = Modifier.fillMaxWidth().height(160.dp)
            )
        }
    }
}