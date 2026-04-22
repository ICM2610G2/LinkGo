package com.friendevs.linkgo.ui.feature.chat


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.friendevs.linkgo.R
import com.friendevs.linkgo.domain.model.GroupSummary
import com.friendevs.linkgo.ui.navigation.Screens

@Composable
fun ChatScreen(
    navController: NavController,
    model: ChatViewModel = viewModel()
) {
    val state by model.state.collectAsState()

    ChatContent(
        state = state,
        onGroupClick = {
            navController.navigate(Screens.ChatDetail.name)
        }
    )
}

@Composable
private fun ChatContent(
    state: ChatState,
    onGroupClick: (String) -> Unit
) {
    Scaffold(
        topBar = { topBarChat() },
        floatingActionButton = {
            floatingButtonChat()
        }
    )
    { paddingValues ->

        //------------------CONTENIDO PRINCIPAL------------------

        // fitros
        Column(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxSize()
        ) {

            Row(
                modifier = Modifier
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text = "All",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable(onClick = {})
                )


                Text(
                    text = "Groups",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable(onClick = {})
                )

                Text(
                    text = "Todos",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable(onClick = {})
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
            if (state.groups.isEmpty()) {
                Text(
                    text = "Aun no hay grupos",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }

}

@Composable
private fun GroupCard(
    group: GroupSummary,
    onClick: () -> Unit
) {
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
                Image(
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = "Foto",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                )
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
fun floatingButtonChat() {
    FloatingActionButton(
        onClick = { },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape
    ) {
        Icon(Icons.Default.Create, contentDescription = "Nuevo Mensaje")
    }
}
