package com.friendevs.linkgo.screens
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.friendevs.linkgo.R
import com.friendevs.linkgo.navigation.Screens
import coil3.compose.AsyncImage
import kotlin.collections.emptyList

@Composable
@ExperimentalMaterial3Api
fun ProfileScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        topBar = {UserTopAppBar()}
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp)

        ) {
            item(){ ProfileHeader() }
            item() {ProfileEditRow()}
            item() { StatsRow() }
            item() {MomentsGrid()}

        }
    }
}

@Composable
fun ProfileEditRow(){
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {},
                Modifier.width(270.dp).height(50.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    MaterialTheme
                        .colorScheme.primary
                )
            )
            {
                Text("Editar Perfil", fontWeight = FontWeight.Bold, fontSize =18.sp)
            }

            Button(
                onClick = {},
                modifier = Modifier.size(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

}

@Composable
fun ProfileHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        ProfileAvatar()
        Text("Nicolas", style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold, fontSize = 30.sp)

        Text(
            "@ndgc",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary, fontSize = 18.sp
        )
    }
}

@Composable
fun StatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {

        StatItem("3", "Fotos")
        StatItem("12", "Amigos")
        StatItem("14", "Circulos")
    }
}

@Composable
fun StatItem(number: String, label: String) {
        Card(
            modifier = Modifier.width(100.dp).height(80.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors
                (MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    number, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                    fontSize = 25.sp
                )
                Text(label, color = Color.Gray)
            }
        }

}


@Composable
fun BottomNavigationBar(navController: NavHostController) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface) {

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
                selected = true,
                onClick = { navController.navigate(Screens.Profile.name) },
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                label = { Text("PROFILE") }
            )
        }
}

@Composable
@ExperimentalMaterial3Api
fun UserTopAppBar(){
    TopAppBar(
        title = {Text("Perfil", fontSize = 28.sp,
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Settings, "Profile Settings")
            }
        })
}


@Composable
fun ProfileAvatar() {

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier.size(140.dp)
    ) {

        Image(
            painter = painterResource(R.drawable.nico1), // tu imagen
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(
                    width = 4.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun MomentsGrid() {
    Row(
        Modifier.fillMaxWidth().padding(13.dp),
        horizontalArrangement = Arrangement.Start,

    ){

        Text("Mis Momentos", style = MaterialTheme.typography.titleMedium)

    }

    val context = LocalContext.current

    val imagePaths = remember {
        val files = context.assets.list("selfpics")

        if (files != null) {
            files.map { "selfpics/$it" }
        }else{
            emptyList()
        }


    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.height(300.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(4.dp)
    ) {
        items(imagePaths) { path ->
            AsyncImage(
                model = "file:///android_asset/$path",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
    }
}
