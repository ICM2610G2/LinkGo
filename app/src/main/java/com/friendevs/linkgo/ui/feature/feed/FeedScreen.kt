package com.friendevs.linkgo.ui.feature.feed

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.friendevs.linkgo.R
import com.friendevs.linkgo.domain.model.Contacts
import com.friendevs.linkgo.ui.navigation.Screens

@Composable
fun FeedScreen(navController: NavController) {
    Scaffold(
        topBar = { TopBarFeed() }
    )
    { paddingValues ->

        Column(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxSize()
        ) {

            // Filtros
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
                    text = "Chats",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable(onClick = {})
                )

                Text(
                    text = "Circulos",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clickable(onClick = {})
                )
            }


            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(Contacts) { contact ->
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
                                Image(
                                    painter = painterResource(id = R.drawable.img),
                                    contentDescription = "Foto de perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(CircleShape)
                                )


                                Column(
                                    modifier = Modifier
                                        .padding(start = 12.dp)
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = contact.fullName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = contact.time,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }


                            Image(
                                painter = painterResource(id = R.drawable.foto_feed),
                                contentDescription = "Imagen del post",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp) 
                            )
                            PostActions()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarFeed() {
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
                    painter = painterResource(id = R.drawable.photo_camera),
                    contentDescription = "Cámara",
                    modifier = Modifier.size(20.dp)
                )
            }
        },
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
fun PostActions() {

    var liked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            onClick = { liked = !liked }
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Like",
                tint = if (liked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
            )
        }

        Text(
            text = if (liked) "Te gusta" else "Me gusta",
            fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FeedPreview() {
    FeedScreen(navController = NavController(LocalContext.current))
}