package com.friendevs.linkgo.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.friendevs.linkgo.model.Hotspot
import androidx.compose.ui.graphics.Color
import coil3.compose.AsyncImage

@Composable
fun HotspotCard(hotspot: Hotspot) {

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF1E1639)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = hotspot.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = "Fotos: ${hotspot.fotos}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White
            )
            AsyncImage(
                model = hotspot.url,
                contentDescription = hotspot.name,
                modifier = Modifier.fillMaxWidth().height(160.dp)
            )
        }
    }
}