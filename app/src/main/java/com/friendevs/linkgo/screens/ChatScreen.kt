package com.friendevs.linkgo.screens


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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.friendevs.linkgo.R
import com.friendevs.linkgo.model.Contacts
import com.friendevs.linkgo.navigation.Screens

@Composable
fun ChatScreen(navController: NavController) {
    Scaffold(
        topBar = { topBarChat() },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screens.Map.name) },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    label = { Text("MAP") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screens.Feed.name) },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    label = { Text("FEED") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { navController.navigate(Screens.Chat.name) },
                    icon = { Icon(Icons.Default.Send, contentDescription = null) },
                    label = { Text("CHAT") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screens.Hotspots.name) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("HOTSPOTS") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screens.Profile.name) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("PROFILE") }
                )
            }
        },
        floatingActionButton = {
            floatingButtonChat()
        }
    )
    { paddingValues ->

        //------------------CONTENIDO PRINCIPAL------------------

        // fitros
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            Row(
                modifier = Modifier
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Text(
                    text = "All",
                    color = Color.White,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable(onClick = {})
                )


                Text(
                    text = "Chats",
                    color = Color.White,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable(onClick = {})
                )

                Text(
                    text = "Circulos",
                    color = Color.White,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable(onClick = {})
                )
            }
            // CHATS
        LazyColumn(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(Contacts) { contact ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .height(80.dp)
                        .clickable(onClick = { navController.navigate(Screens.ChatDetail.name) }),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                {
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
                                horizontalArrangement =
                                    Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {

                                Text(contact.fullName, fontSize = 20.sp)
                                Text(contact.time, fontSize = 15.sp)

                            }
                            Row() {
                                Text(
                                    contact.message,
                                    fontSize = 15.sp
                                )
                            }

                        }
                    }
                }

            }
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
                    tint = Color.White,
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

@Preview(showSystemUi = true)
@Composable
fun chatPreview() {
    ChatScreen(navController = NavController(LocalContext.current))

}
