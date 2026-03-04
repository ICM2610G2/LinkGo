package com.friendevs.linkgo.screens

import android.R.color.white
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.friendevs.linkgo.R
import com.friendevs.linkgo.navigation.Screens

@Composable
fun MapScreen(navController: NavController) {

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    selected = true,
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
                    selected = false,
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

        }

    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
            // Esto hace que pueda poner la imagen hasta arriba pero el padding de abajo si lo usa
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                // Filtro


                Image(
                    painter = painterResource(R.drawable.mapa),
                    contentDescription = "mapa",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.FillBounds
                )

                Row( // Filtros
                    modifier = Modifier
                        .padding(top = 65.dp).align(Alignment.TopCenter),
                ) {

                    Text(
                        text = "All",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clickable(onClick = {})
                            .width(62.dp)
                    )


                    Text(
                        text = "Chats",
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.surface, CircleShape)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clickable(onClick = {})
                            .width(62.dp)
                    )

                    Text(
                        text = "Circulos",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(color = MaterialTheme.colorScheme.surface, CircleShape)
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .clickable(onClick = {})
                            .width(62.dp)


                    )
                }

                Button(
                    onClick = { navController.navigate(Screens.MeetUp.name) },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 20.dp, bottom = 35.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Meet up"
                        )
                        Text(
                            text = "Meet Up"
                        )
                    }
                }

                Button(onClick = {},
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 35.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary)
                )
                {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location"
                    )
                }
                 // Personas
                Image(
                    painter = painterResource(R.drawable.andres),
                    contentDescription = "Personita",
                    contentScale = ContentScale.Crop, // la foto llena el círculo
                    modifier = Modifier
                        .offset(x = -65.dp, y = -185.dp)
                        .size(60.dp) // tamaño del círculo
                        .clip(CircleShape) // Lo hace redondo
                        .border(2.dp, Color(0xFF907CFC), CircleShape) //bordecito morado
                )

                Image(
                    painter = painterResource(R.drawable.lucho),
                    contentDescription = "Personita",
                    contentScale = ContentScale.Crop, // la foto llena el círculo
                    modifier = Modifier
                        .offset(x = 120.dp, y = 50.dp)
                        .size(60.dp) // tamaño del círculo
                        .clip(CircleShape) // Lo hace redondo
                        .border(2.dp, Color(0xFF907CFC), CircleShape) //bordecito morado
                )

                Image(
                    painter = painterResource(R.drawable.nico1),
                    contentDescription = "Personita",
                    contentScale = ContentScale.Crop, // la foto llena el círculo
                    modifier = Modifier
                        .offset(x = 30.dp, y = 0.dp)
                        .size(60.dp) // tamaño del círculo
                        .clip(CircleShape) // Lo hace redondo
                        .border(2.dp, Color(0xFF907CFC), CircleShape) //bordecito morado
                )

                Image(
                    painter = painterResource(R.drawable.nico2),
                    contentDescription = "Personita",
                    contentScale = ContentScale.Crop, // la foto llena el círculo
                    modifier = Modifier
                        .offset(x = 40.dp, y = -130.dp)
                        .size(60.dp) // tamaño del círculo
                        .clip(CircleShape) // Lo hace redondo
                        .border(2.dp, Color(0xFF907CFC), CircleShape) //bordecito morado
                )

                Image(
                    painter = painterResource(R.drawable.juanes),
                    contentDescription = "Personita",
                    contentScale = ContentScale.Crop, // la foto llena el círculo
                    modifier = Modifier
                        .offset(x = -90.dp, y = 210.dp)
                        .size(60.dp) // tamaño del círculo
                        .clip(CircleShape) // Lo hace redondo
                        .border(2.dp, Color(0xFF907CFC), CircleShape) //bordecito morado
                )

            }




        }

    }

}

@Composable
@Preview
fun mostrarMap() {
    MapScreen(navController = NavController(LocalContext.current))
}