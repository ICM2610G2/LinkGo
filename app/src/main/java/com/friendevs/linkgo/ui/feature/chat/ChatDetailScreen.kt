package com.friendevs.linkgo.ui.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Send
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

import androidx.compose.material3.Divider
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatDetailScreen(
    navController: NavController,
    groupId: String,
    sensorViewModel: com.friendevs.linkgo.model.SensorViewModel,
    viewModel: ChatDetailViewModel = viewModel()
) {
    val bgTop = MaterialTheme.colorScheme.background
    val bgBottom = MaterialTheme.colorScheme.surface
    val bubbleMe = MaterialTheme.colorScheme.primary
    val bubbleOther = MaterialTheme.colorScheme.surfaceVariant
    val divider = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)

    LaunchedEffect(groupId) {
        viewModel.start(groupId)
    }

    val messages = viewModel.messages
    val currentUid = viewModel.currentUid
    val group = viewModel.group
    val groupName = group?.name?.ifBlank { "Grupo" } ?: "Cargando..."
    val inviteCode = group?.codigoInvitacion ?: ""
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val clipboardManager = LocalClipboardManager.current

    var input by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                    ChatDetailTopBar(
                    name = groupName,
                    inviteCode = inviteCode,
                    onBack = { navController.popBackStack() },
                    onCopyCode = {
                        if (inviteCode.isNotBlank()) {
                            clipboardManager.setText(AnnotatedString(inviteCode))
                        }
                    },
                    dividerColor = divider
                )
            },
            bottomBar = {
                ChatDetailInputBar(
                    value = input,
                    onValueChange = { input = it },
                    onSend = {
                        val trimmed = input.trim()
                        if (trimmed.isNotEmpty()) {
                            viewModel.send(trimmed)
                            input = ""
                        }
                    },
                    dividerColor = divider
                )
            },
            containerColor = Color.Transparent,
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item { DayChip(text = "HOY") }

                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(
                            fromMe = msg.senderId == currentUid,
                            text = msg.text,
                            time = if (msg.timestamp > 0) timeFormat.format(Date(msg.timestamp)) else "",
                            bubbleMe = bubbleMe,
                            bubbleOther = bubbleOther
                        )
                    }
                }
            }
        }

        if (sensorViewModel.proximityNear){
            Box(modifier = Modifier.fillMaxSize().background(Color.Black).clickable(enabled = false){}) {
                Text(text = "Modo Privado",
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ChatDetailTopBar(
    name: String,
    inviteCode: String,
    onBack: () -> Unit,
    onCopyCode: () -> Unit,
    dividerColor: Color,
) {
    // generate a deterministic color from the group name
    val avatarColor = remember(name) {
        val hue = (name.hashCode().and(0xFF)) / 255f * 360f
        Color.hsl(hue, 0.45f, 0.50f)
    }
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "G"

    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // Avatar con inicial del grupo
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Código de invitación siempre visible con botón de copiar
                if (inviteCode.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(onClick = onCopyCode)
                            .wrapContentWidth()
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Código: $inviteCode",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "copiar",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }


            }

            Divider(color = dividerColor)
        }
    }

@Composable
private fun DayChip(text: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), shape = RoundedCornerShape(20.dp)) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MessageBubble(
    fromMe: Boolean,
    text: String,
    time: String,
    bubbleMe: Color,
    bubbleOther: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (fromMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!fromMe) {
            MiniAvatar()
            Spacer(Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (fromMe) Alignment.End else Alignment.Start) {
            Surface(
                color = if (fromMe) bubbleMe else bubbleOther,
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = text,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (time.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = time,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun MiniAvatar() {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Face,
            contentDescription = "avatar",
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ChatDetailInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    dividerColor: Color,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Divider(color = dividerColor)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(22.dp)
            ) {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            "Escribe un mensaje...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.onSurface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 3
                )
            }

            Spacer(Modifier.width(10.dp))

            FilledIconButton(
                onClick = onSend,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(imageVector = Icons.Filled.Send, contentDescription = "Send")
            }
        }
    }
}