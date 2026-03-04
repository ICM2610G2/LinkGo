package com.friendevs.linkgo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Send

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@Composable
fun ChatDetailScreen(navController: NavController) {
    // Colores aproximados a tu mockup (morado/oscuro)
    val bgTop = Color(0xFF0B0A14)
    val bgBottom = Color(0xFF1A1233)
    val bubbleMe = Color(0xFF6C4DFF)
    val bubbleOther = Color(0xFF2A1F50)
    val divider = Color.White.copy(alpha = 0.08f)

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "1",
                fromMe = false,
                kind = MessageKind.Text(
                    "¡Hola! ¿Seguimos quedando en el parque para la sesión de fotos del atardecer?"
                ),
                time = "14:02"
            ),
            ChatMessage(
                id = "2",
                fromMe = true,
                kind = MessageKind.Text(
                    "¡Sí! Ya estoy aquí. La iluminación es absolutamente perfecta ahora mismo. 📸"
                ),
                time = "14:05"
            ),
            ChatMessage(
                id = "3",
                fromMe = false,
                kind = MessageKind.ImageWithCaption(
                    imageUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=1200&q=80",
                    caption = "¡Vaya, tienes razón! Mira esto desde mi balcón."
                ),
                time = "14:06"
            ),
            ChatMessage(
                id = "4",
                fromMe = true,
                kind = MessageKind.Text("jajajaj"),
                time = ""
            ),
        )
    }

    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            ChatDetailTopBar(
                name = "Sofia",
                status = "En línea",
                onBack = { navController.popBackStack() },
                onVideoCall = { /* TODO */ },
                onCall = { /* TODO */ },
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
                        messages.add(
                            ChatMessage(
                                id = System.currentTimeMillis().toString(),
                                fromMe = true,
                                kind = MessageKind.Text(trimmed),
                                time = ""
                            )
                        )
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
                    when (val k = msg.kind) {
                        is MessageKind.Text -> MessageBubble(
                            fromMe = msg.fromMe,
                            text = k.value,
                            time = msg.time,
                            bubbleMe = bubbleMe,
                            bubbleOther = bubbleOther
                        )

                        is MessageKind.ImageWithCaption -> ImageMessageBubble(
                            fromMe = msg.fromMe,
                            imageUrl = k.imageUrl,
                            caption = k.caption,
                            time = msg.time,
                            bubbleOther = bubbleOther
                        )
                    }
                }
            }
        }
    }
}

private data class ChatMessage(
    val id: String,
    val fromMe: Boolean,
    val kind: MessageKind,
    val time: String
)

private sealed class MessageKind {
    data class Text(val value: String) : MessageKind()
    data class ImageWithCaption(val imageUrl: String, val caption: String) : MessageKind()
}

@Composable
private fun ChatDetailTopBar(
    name: String,
    status: String,
    onBack: () -> Unit,
    onVideoCall: () -> Unit,
    onCall: () -> Unit,
    dividerColor: Color,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                    tint = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2B2246)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Face,
                    contentDescription = "Avatar",
                    tint = Color.White.copy(alpha = 0.85f)
                )

                // puntito verde online
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38D46A))
                        .border(2.dp, Color(0xFF0B0A14), CircleShape)
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = status,
                    color = Color.White.copy(alpha = 0.65f),
                    style = MaterialTheme.typography.bodySmall
                )
            }


            IconButton(onClick = onCall) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = "Call",
                    tint = Color(0xFF8B77FF)
                )
            }
        }

        Divider(color = dividerColor)
    }
}

@Composable
private fun DayChip(text: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(color = Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(20.dp)) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color.White.copy(alpha = 0.75f),
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
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (time.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = time,
                    color = Color.White.copy(alpha = 0.35f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun ImageMessageBubble(
    fromMe: Boolean,
    imageUrl: String,
    caption: String,
    time: String,
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
            Surface(color = bubbleOther, shape = RoundedCornerShape(18.dp)) {
                Column(modifier = Modifier.padding(10.dp)) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Shared photo",
                        modifier = Modifier
                            .size(width = 240.dp, height = 150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.15f)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = caption,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            if (time.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = time,
                    color = Color.White.copy(alpha = 0.35f),
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
            .background(Color(0xFF2B2246)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Face,
            contentDescription = "avatar",
            tint = Color.White.copy(alpha = 0.8f),
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
            FilledIconButton(
                onClick = { /* TODO: adjuntar */ },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.08f),
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Add")
            }

            Spacer(Modifier.width(10.dp))

            Surface(
                modifier = Modifier.weight(1f),
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                "Escribe un mensaje...",
                                color = Color.White.copy(alpha = 0.45f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        maxLines = 3
                    )

                    IconButton(onClick = { /* TODO: emojis */ }) {
                        Icon(
                            imageVector = Icons.Filled.Face,
                            contentDescription = "Emoji",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            FilledIconButton(
                onClick = onSend,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color(0xFF6C4DFF),
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Filled.Send, contentDescription = "Send")
            }
        }
    }
}