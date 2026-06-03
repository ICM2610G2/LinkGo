package com.friendevs.linkgo.ui.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.friendevs.linkgo.domain.model.GroupSummary
import com.friendevs.linkgo.ui.navigation.Screens

@Composable
fun ChatScreen(
    navController: NavController,
    model: ChatViewModel = viewModel()
) {
    val state by model.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    ChatContent(
        state = state,
        onGroupClick = { groupId ->
            navController.navigate("${Screens.ChatDetail.name}/$groupId")
        },
        onFabClick = { showDialog = true }
    )

    if (showDialog) {
        GroupDialog(
            onDismiss = { showDialog = false },
            onCreate = { name, desc ->
                model.createGroup(name, desc)
                showDialog = false
            },
            onJoin = { code ->
                model.joinGroup(code) { }
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatContent(
    state: ChatState,
    onGroupClick: (String) -> Unit,
    onFabClick: () -> Unit
) {
    Scaffold(
        topBar = { topBarChat() },
        floatingActionButton = {
            floatingButtonChat(onClick = onFabClick)
        }
    )
    { paddingValues ->

        //------------------CONTENIDO PRINCIPAL------------------

        Column(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxSize()
        ) {

            if (state.groups.isEmpty()) {
                Text(
                    text = "Aun no hay grupos",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.groups, key = { it.id }) { group ->
                    GroupCard(
                        group = group,
                        onClick = { onGroupClick(group.id) }
                    )
                }
            }
        }
    }

}

@Composable
private fun GroupCard(
    group: GroupSummary,
    onClick: () -> Unit
) {
    val avatarColor = remember(group.name) {
        val hue = (group.name.hashCode().and(0xFF)) / 255f * 360f
        Color.hsl(hue, 0.45f, 0.50f)
    }
    val initial = group.name.firstOrNull()?.uppercaseChar()?.toString() ?: "G"

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .height(88.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
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
            }

            Column(
                modifier = Modifier
                    .weight(9f)
                    .fillMaxHeight()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center

            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = group.name.ifBlank { "Grupo sin nombre" },
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${group.membersCount} miembros",
                        fontSize = 12.sp
                    )
                }

                val subtitle = if (group.descripcion.isBlank()) {
                    "Codigo: ${group.codigoInvitacion.ifBlank { "N/A" }}"
                } else {
                    group.descripcion
                }

                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun topBarChat() {
    TopAppBar(
        title = {
            Text(
                text = "Mensajes",
                fontSize = 25.sp,
                fontWeight = FontWeight.Bold,
            )
        },

        //Icono de la barra Menu
        navigationIcon = {

            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        //Icono de busqueda
        actions = {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}


@Composable
fun floatingButtonChat(onClick: () -> Unit = {}) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape
    ) {
        Icon(Icons.Default.Create, contentDescription = "Nuevo Mensaje")
    }
}

@Composable
private fun GroupDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit,
    onJoin: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Grupos") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Crear grupo nuevo", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripcion") },
                    singleLine = true
                )
                Button(
                    onClick = { onCreate(name, descripcion) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Crear") }

                Spacer(Modifier.height(4.dp))
                Text("Unirse por codigo", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Codigo de invitacion") },
                    singleLine = true
                )
                Button(
                    onClick = { onJoin(code) },
                    enabled = code.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Unirse") }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        }
    )
}
